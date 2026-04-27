<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemUserGroupApi } from '#/api/';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteUserGroup,
  exportUserGroupList,
  getUserGroupPage,
  importUserGroupList,
} from '#/api';
import { PERMISSION_ENUM } from '#/enums';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import EditDrawer from './modules/editDrawer.vue';
import Form from './modules/form.vue';

const { hasAccessByCodes } = useAccess();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [EditDrawerComponent, editDrawerApi] = useVbenDrawer({
  connectedComponent: EditDrawer,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
    showCollapseButton: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserGroupPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    importConfig: hasAccessByCodes([PERMISSION_ENUM.SystemUserGroupImport])
      ? {
          types: ['xlsx'],
          modes: ['insertTop'],
          remote: true,
          async importMethod(options: any) {
            await importUserGroupList({ ...options }).then((res) => {
              gridApi.query();
              message.success(res || $t('common.importSuccess'));
            });
          },
        }
      : undefined,
    actionWrapperClass: 'text-center',
  } as VxeTableGridOptions<SystemUserGroupApi.SystemUserGroup>,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemUserGroupApi.SystemUserGroup>) {
  switch (code) {
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'edit': {
      onEdit(row);
      break;
    }
    case 'editInfo': {
      onEditInfo(row);
      break;
    }
    default: {
      break;
    }
  }
}

function onCreate() {
  formModalApi.setData({}).open();
}

function onRefresh() {
  gridApi.query();
}

function onDelete(row: SystemUserGroupApi.SystemUserGroup) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteUserGroup(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.name]),
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onEdit(row: SystemUserGroupApi.SystemUserGroup) {
  formModalApi.setData(row).open();
}

function onEditInfo(row: SystemUserGroupApi.SystemUserGroup) {
  editDrawerApi.setData(row).open();
}
</script>
<template>
  <Page auto-content-height>
    <FormModal @success="onRefresh" />
    <EditDrawerComponent @success="onRefresh" />
    <Grid :table-title="$t('system.userGroup.list')">
      <template #toolbar-tools>
        <Button type="primary" class="mr-2" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.userGroup.name')]) }}
        </Button>
        <VxeTableExportButton
          :api="exportUserGroupList"
          v-access="PERMISSION_ENUM.SystemUserGroupExport"
        />
      </template>
    </Grid>
  </Page>
</template>
