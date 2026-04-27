<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { VxeTableEditingCell, VxeTableExportButton } from '#/adapter/component';
import { getFilterParams, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportAssets,
  getAssetsPage,
  importAssets,
  syncAssets,
  updateAssets,
} from '#/api';
import { PERMISSION_ENUM } from '#/enums';
import { useMultiRequest } from '#/hooks';
import { $t } from '#/locales';

import { useColumns } from './data';

const { hasAccessByCodes } = useAccess();

const EDITABLE_FIELDS = [
  'assetMajorCategoryCode',
  'assetMajorCategoryName',
  'erpAssetType',
  'assetTypeName',
  'year',
];

const [pageService, syncAssetsService] = useMultiRequest([
  {
    request: getAssetsPage,
  },
  {
    request: syncAssets,
    config: {
      onSuccess: (res: any) => {
        message.success(res || $t('common.syncSuccess'));
        gridApi.query();
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
        if (row.loading?.[column.field]) {
          return false;
        }
        return true;
      },
    },
    proxyConfig: {
      filter: true,
      ajax: {      
        query: async ({ page, filters }) => {
          const filterParams = getFilterParams(filters);
          return await pageService
            ?.run({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              ...filterParams,
            })
            .then((res) => {
              res.list.forEach((item: any) => {
                const loading: Record<string, boolean> = {};
                const editError: Record<string, string | null> = {};
                EDITABLE_FIELDS.forEach((field) => {
                  loading[field] = false;
                  editError[field] = null;
                });
                item.loading = loading;
                item.editError = editError;
              });
              return res;
            });
        },
      },
    },
    importConfig: hasAccessByCodes([PERMISSION_ENUM.ImportAssetsImport])
      ? {
          types: ['xlsx'],
          modes: ['covering'],
          remote: true,
          async importMethod(options: any) {
            await importAssets({ ...options }).then((res: any) => {
              gridApi.query();
              message.success(res || $t('common.importSuccess'));
            });
          },
        }
      : undefined,
    actionWrapperClass: 'text-center',
  } as VxeTableGridOptions<any>,
  gridEvents: {
    // filterChange({ filterList }: { filterList: any[] }) {
    //   const params: Recordable<any> = {};
    //   filterList.forEach((item) => {
    //     const val = item.datas?.[0];
    //     params[item.field] = val?.value ?? val ?? undefined;
    //   });
    //   queryParams.value = params;
    //   gridApi.query();
    // },
    async editClosed(table: any) {
      const { column, row, $grid } = table;
      if ($grid.isUpdateByRow(row)) {
        if (!row.loading) {
          row.loading = Object.fromEntries(
            EDITABLE_FIELDS.map((f) => [f, false]),
          );
        }
        if (!row.editError) {
          row.editError = Object.fromEntries(
            EDITABLE_FIELDS.map((f) => [f, null]),
          );
        }

        row.loading[column.field] = true;
        await updateAssets({
          id: row.id,
          assetMajorCategoryCode: row.assetMajorCategoryCode,
          assetMajorCategoryName: row.assetMajorCategoryName,
          erpAssetType: row.erpAssetType,
          assetTypeName: row.assetTypeName,
          year: row.year,
        })
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

async function onSync() {
  await syncAssetsService?.run();
}
</script>
<template>
  <Page auto-content-height :description="$t('import.assets.description')">
    <template #extra>
      <Button
        type="default"
        danger
        :loading="syncAssetsService?.loading.value"
        @click="onSync"
      >
        {{ $t('common.sync') }}
      </Button>
    </template>
    <Grid>
      <template #table-title>
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
          :api="exportAssets"
          v-access="PERMISSION_ENUM.ImportAssetsExport"
        />
      </template>
      <template #text="{ row, column }">
        <VxeTableEditingCell
          :loading="row.loading?.[column.field]"
          :error="row.editError?.[column.field]"
        >
          <span>{{ row[column.field] || '-' }}</span>
        </VxeTableEditingCell>
      </template>
      <template #changeStatus="{ row }">
        <span>{{
          row.changeStatus === 'UNCHANGED'
            ? $t('import.assets.unchanged')
            : row.changeStatus === 'NEW'
              ? $t('import.assets.new')
              : row.changeStatus === 'MODIFY'
                ? $t('import.assets.modify')
                : row.changeStatus || '-'
        }}</span>
      </template>
    </Grid>
  </Page>
</template>
