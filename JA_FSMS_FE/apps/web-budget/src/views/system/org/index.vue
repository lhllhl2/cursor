<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemOrgApi } from '#/api/';

// import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import {
  VxeTableExpandTreeButton,
  VxeTableExportButton,
  VxeTableImportButton,
} from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportOrgList,
  getOrgList,
  importOrgList,
  orgSync,
} from '#/api/system/org';
import { PERMISSION_ENUM } from '#/enums';
import { useMultiRequest } from '#/hooks';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

// 使用 useMultiRequest 管理多个请求
const [orgSyncService, orgListService] = useMultiRequest([
  {
    request: orgSync,
    config: {
      onSuccess: () => {
        message.success($t('system.org.syncSuccess'));
        onRefresh();
      },
    },
  },
  {
    request: getOrgList,
  },
]);

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

// const { hasAccessByCodes } = useAccess();

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
    showCollapseButton: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    // keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_evnet, formValues) =>
          orgListService?.run({ ...formValues }),
        querySuccess: (grid: any, formValues: any) => {
          const isSearch = formValues.name || formValues.code;
          if (isSearch) {
            grid.$grid.setAllTreeExpand(true);
          }
        },
      },
    },
    // importConfig: hasAccessByCodes([PERMISSION_ENUM.SystemOrgImport])
    //   ? {
    //       types: ['xlsx'],
    //       modes: ['covering'],
    //       remote: true,
    //       async importMethod(options: any) {
    //         await importOrgList({ ...options }).then((res) => {
    //           gridApi.query();
    //           message.success(res || $t('common.importSuccess'));
    //         });
    //       },
    //     }
    //   : undefined,
    scrollY: {
      enabled: true,
      gt: 0,
    },
    treeConfig: {
      parentField: 'pcode',
      rowField: 'code',
      transform: true,
    },
  } as VxeTableGridOptions<SystemOrgApi.SystemOrg>,
});

function onActionClick(e: OnActionClickParams<SystemOrgApi.SystemOrg>) {
  switch (e.code) {
    case 'auth': {
      onAuth(e.row);
      break;
    }
  }
}

function onAuth(row: SystemOrgApi.SystemOrg) {
  formDrawerApi.setData(row).open();
}

function onBatchAuth() {
  const selectRecords = gridApi.grid.getCheckboxRecords();
  if (selectRecords.length > 0) {
    // 如果选中的组织有子组织，则打开子组织的授权页面
    const org = selectRecords.filter(
      (item: SystemOrgApi.SystemOrg) => item?.children?.length === 0,
    );
    formDrawerApi.setData(org).open();
  } else {
    message.warning($t('common.table_pleaseSelect'));
  }
}

function onRefresh() {
  gridApi.query();
}

function onSync() {
  orgSyncService?.run();
}
</script>
<template>
  <Page auto-content-height :description="$t('system.org.description')">
    <template #extra>
      <Button
        type="default"
        danger
        :loading="orgSyncService?.loading.value"
        @click="onSync"
      >
        {{ $t('system.org.sync') }}
      </Button>
    </template>
    <FormDrawer @success="onRefresh" />
    <Grid :table-title="$t('system.org.list')">
      <template #toolbar-tools>
        <Button
          type="primary"
          class="mr-2"
          @click="onBatchAuth"
          v-access="PERMISSION_ENUM.SystemOrgBatchAuth"
        >
          <IconifyIcon icon="carbon:two-factor-authentication" />
          {{ $t('common.batch', [$t('system.org.auth')]) }}
        </Button>
        <VxeTableExpandTreeButton class="mr-2" :grid="gridApi.grid" />
        <VxeTableExportButton
          class="mr-2"
          :api="exportOrgList"
          v-access="PERMISSION_ENUM.SystemOrgExport"
        />
        <VxeTableImportButton
          :api="importOrgList"
          :grid-api="gridApi"
          v-access="PERMISSION_ENUM.SystemOrgImport"
        />
      </template>
    </Grid>
  </Page>
</template>
