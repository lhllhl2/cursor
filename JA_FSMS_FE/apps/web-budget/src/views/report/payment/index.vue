<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import dayjs from 'dayjs';

import { VxeTableExportButton } from '#/adapter/component';
import { getFilterParams, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportPaymentStatus,
  fetchDictByCode,
  queryPaymentStatus,
} from '#/api';
import { DIC_ENUM } from '#/enums/dictEnum';

import { useColumns } from './data';

const internalFilters = ref<Array<{ label: string; value: string }>>([]);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    // columns: useColumns(),
    height: 'auto',
    keepSource: true,
    filterConfig: {
      remote: true,
    },
    proxyConfig: {
      filter: true,
      ajax: {
        query: async ({ page, filters }) => {
          // 如果筛选器中没有年份，则使用当前年份作为默认值
          const filterParams = getFilterParams(filters);
          const backendParams = {
            year: String(dayjs().year()),
            ...filterParams,
          };
          const response = await queryPaymentStatus({
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
});

async function loadDict() {
  const res = await fetchDictByCode({ code: DIC_ENUM.INTERNAL });
  internalFilters.value = res;
  // 更新当前表格的列配置
  updateColumnsFilters();
}

function updateColumnsFilters() {
  const columns = useColumns();
  const internalColumn = columns?.find((col) => col.field === 'isInternal');

  if (internalColumn) {
    internalColumn.filters = internalFilters.value;
  }
  gridApi.setGridOptions({
    columns,
  });
}

/** 导出参数 */
const exportParams = async () => {
  const filters = await gridApi.grid?.getCheckedFilters();
  const filterParams = getFilterParams(filters);
  return {
    year: String(dayjs().year()),
    ...filterParams,
  };
};

onMounted(() => {
  loadDict();
});
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #toolbar-tools>
        <VxeTableExportButton
          :api="exportPaymentStatus"
          :params="exportParams"
        />
      </template>
    </Grid>
  </Page>
</template>

<style lang="less" scoped></style>
