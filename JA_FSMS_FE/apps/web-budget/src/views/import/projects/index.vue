<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Checkbox, message, Select } from 'ant-design-vue';
import dayjs from 'dayjs';

import { VxeTableEditingCell, VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportProjects,
  getProjectsPage,
  importProjects,
  syncProjectApiData,
  syncProjects,
  updateProjects,
} from '#/api';
import { PERMISSION_ENUM } from '#/enums';
import { useMultiRequest } from '#/hooks';
import { $t } from '#/locales';
import { yearOptions } from '#/utils';

import { useColumns } from './data';

const year = ref(dayjs().format('YYYY'));
const queryParams = ref({});

const { hasAccessByCodes } = useAccess();

const [pageService, syncProjectsService, apiSyncService] = useMultiRequest([
  {
    request: getProjectsPage,
  },
  {
    request: syncProjects,
    config: {
      onSuccess: () => {
        message.success($t('common.syncSuccess'));
        gridApi.query();
      },
    },
  },
  {
    request: syncProjectApiData,
    config: {
      onSuccess: () => {
        message.success($t('common.syncSuccess'));
        // gridApi.query();
      },
    },
  },
]);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    filterConfig: {
      remote: true,
    },
    editConfig: {
      trigger: 'dblclick',
      mode: 'cell',
      showStatus: true,
      beforeEditMethod(table: any) {
        const { row, column } = table;
        if (row.loading[column.field]) {
          return false;
        }
        return true;
      },
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await pageService
            ?.run({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              ...queryParams.value,
              year: year.value,
            })
            .then((res) => {
              res.list.forEach((item: any) => {
                item.loading = {
                  controlLevel: false,
                };
                item.editError = {
                  controlLevel: null,
                };
              });
              return res;
            });
        },
      },
    },
    importConfig: hasAccessByCodes([PERMISSION_ENUM.ImportProjectsImport])
      ? {
          types: ['xlsx'],
          modes: ['covering'],
          remote: true,
          async importMethod(options: any) {
            await importProjects({ ...options }).then((res: any) => {
              gridApi.query();
              message.success(res || $t('common.importSuccess'));
            });
          },
        }
      : undefined,
    actionWrapperClass: 'text-center',
  } as VxeTableGridOptions<any>,
  gridEvents: {
    filterChange({ filterList }: { filterList: any[] }) {
      // 不要用reduce
      const params: Recordable<any> = {};
      filterList.forEach((item) => {
        params[item.field] = item.datas[0];
      });
      queryParams.value = {
        ...params,
      };
      gridApi.query();
    },
    async editClosed(table: any) {
      const { column, row, $grid } = table;
      if ($grid.isUpdateByRow(row)) {
        row.loading[column.field] = true;
        row.controlLevel = row.controlLevel ? '1' : null;
        await updateProjects({ ...row })
          .then(() => {
            row.editError[column.field] = null;
            $grid.reloadRow(row, null, column.field);
            message.success($t('ui.actionMessage.operationSuccess'));
          })
          .catch((error: any) => {
            row.editError[column.field] =
              error.msg || `${$t('ui.actionMessage.operationFailed')}`;
          })
          .finally(() => {
            row.loading[column.field] = false;
          });
      }
    },
  },
});

function onYearChange(value: any) {
  year.value = value;
  gridApi.query();
}

async function onSync() {
  await syncProjectsService?.run();
}

function onApiSync() {
  apiSyncService?.run();
}
</script>
<template>
  <Page auto-content-height :description="$t('import.projects.description')">
    <template #extra>
      <Button
        type="primary"
        :loading="apiSyncService?.loading.value"
        @click="onApiSync"
        class="mr-2"
        v-access="PERMISSION_ENUM.ImportProjectsApiSync"
      >
        {{ $t('import.orgs.apiSync') }}
      </Button>
      <Button
        type="default"
        danger
        :loading="syncProjectsService?.loading.value"
        @click="onSync"
      >
        {{ $t('common.sync') }}
      </Button>
    </template>
    <Grid>
      <template #table-title>
        <IconifyIcon icon="carbon:calendar" class="year-icon" />
        <Select
          v-model:value="year"
          :options="yearOptions()"
          :bordered="false"
          class="year-select w-[90px]"
          @select="onYearChange"
        />
        <span class="text-primary flex items-center text-xs">
          <IconifyIcon
            icon="carbon:information-filled"
            class="ml-1 mr-1 inline-block size-4"
          />
          双击有
          <i class="vxe-table-icon-edit ml-1 mr-1 text-lg text-gray-500"></i
          >图标的列可编辑，编辑完成后点击空白处保存！
        </span>
      </template>
      <template #toolbar-tools>
        <VxeTableExportButton
          :api="exportProjects"
          v-access="PERMISSION_ENUM.ImportProjectsExport"
        />
      </template>
      <template #yes="{ row, column }">
        <VxeTableEditingCell
          :loading="row.loading[column.field]"
          :error="row.editError[column.field]"
        >
          <span class="text-primary font-bold">{{
            row[column.field] === '1' ? '✓' : '-'
          }}</span>
        </VxeTableEditingCell>
      </template>
      <template #edit_bool="{ row, column }">
        <Checkbox v-model:checked="row[column.field]" />
      </template>
    </Grid>
  </Page>
</template>
<style lang="less" scoped>
.year-select {
  :deep(.ant-select-selector) {
    padding: 0 4px;
  }

  :deep(.ant-select-selection-item) {
    // color: #1890ff;
    font-weight: 500;
  }
}

.year-icon {
  font-size: 18px;
  // color: #1890ff;
  flex-shrink: 0;
}
</style>
