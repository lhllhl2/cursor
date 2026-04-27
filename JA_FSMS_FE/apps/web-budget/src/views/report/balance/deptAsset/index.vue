<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type { TableType } from './data';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref, watch } from 'vue';

import { Page } from '@vben/common-ui';

import { Segmented } from 'ant-design-vue';

import { VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  DeptAssetBudgetType,
  exportDeptAssetBudgetBalance,
  queryDeptAssetBudgetBalance,
} from '#/api';

import { useColumnsByType, useTableOptions } from './data';

const currentType = ref<TableType>(DeptAssetBudgetType.PURCHASE);
const queryParams = ref<Recordable<any>>({});

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumnsByType(currentType.value),
    height: 'auto',
    keepSource: true,
    filterConfig: {
      remote: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const backendParams = {
            budgetType: currentType.value,
            ...queryParams.value,
          };
          const response = await queryDeptAssetBudgetBalance({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...backendParams,
          });
          return {
            list: response.list,
            total: response.total,
          };
        },
      },
    },
  } as VxeTableGridOptions<any>,
  gridEvents: {
    filterChange({ filterList }: { filterList: any[] }) {
      const params: Recordable<any> = {};
      filterList.forEach((item) => {
        if (item.datas[0]) {
          params[item.field] = item.datas[0];
        } else if (item.values[0]) {
          params[item.field] = item.values;
        }
      });
      queryParams.value = { ...params };
      gridApi.query();
    },
  },
});

/** 切换表格类型 */
function onTypeChange(value: number | string) {
  const type = value as TableType;
  currentType.value = type;
  queryParams.value = {};
  // 更新列配置
  gridApi.setGridOptions({
    columns: useColumnsByType(type),
  });
  // 重新查询数据
  gridApi.reload();
}

/** 监听类型变化，清除筛选条件 */
watch(currentType, () => {
  gridApi.grid?.clearFilter();
});

/** 导出参数 */
const exportParams = () => {
  return {
    budgetType: currentType.value,
    ...queryParams.value,
  };
};
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #table-title>
        <Segmented
          v-model:value="currentType"
          :options="useTableOptions()"
          @change="onTypeChange"
        />
      </template>
      <template #toolbar-tools>
        <VxeTableExportButton
          :api="exportDeptAssetBudgetBalance"
          :params="exportParams"
        />
      </template>
    </Grid>
  </Page>
</template>

<style lang="less" scoped></style>
