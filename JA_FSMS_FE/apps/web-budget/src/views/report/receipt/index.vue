<script lang="ts" setup>
import type { TableType } from './data';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { onMounted, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';

import { Segmented } from 'ant-design-vue';

import { VxeTableExportButton } from '#/adapter/component';
import { getFilterParams, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  BizType,
  exportReceipt,
  fetchDictByCode,
  queryReceiptData,
} from '#/api';
import { DIC_ENUM } from '#/enums/dictEnum';

import { $t } from '#/locales';

import MultipleNosCell from './components/MultipleNosCell.vue';
import { useColumnsByType, useTableOptions } from './data';

const currentType = ref<TableType>(BizType.APPLY);
const statusFilters = ref<Array<{ label: string; value: string }>>([]);
const internalFilters = ref<Array<{ label: string; value: string }>>([]);
const effectTypeFilters = ref<Array<{ label: string; value: string }>>([]);
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumnsByType(currentType.value),
    height: 'auto',
    keepSource: true,
    filterConfig: {
      remote: true,
    },
    proxyConfig: {
      filter: true,
      ajax: {
        query: async ({ page, filters }) => {
          const filterParams = getFilterParams(filters);
          const backendParams = {
            bizType: currentType.value,
            ...filterParams,
          };
          return await queryReceiptData({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...backendParams,
          });
        },
      },
    },
  } as VxeTableGridOptions<any>,
});

/** 切换表格类型 */
function onTypeChange(value: number | string) {
  const type = value as TableType;
  currentType.value = type;
  // 更新列配置
  updateColumnsFilters();
  // 重新查询数据
  gridApi.reload();
}

async function loadDict() {
  const [receiptStatus, internal, effectTypes] = await Promise.all([
    fetchDictByCode({ code: DIC_ENUM.RECEIPT_STATUS }),
    fetchDictByCode({ code: DIC_ENUM.INTERNAL }),
    fetchDictByCode({ code: DIC_ENUM.EFFECT_TYPE }),
  ]);
  statusFilters.value = receiptStatus;
  internalFilters.value = internal;
  effectTypeFilters.value = effectTypes;
  // 更新当前表格的列配置
  updateColumnsFilters();
}

function updateColumnsFilters() {
  const columns = useColumnsByType(currentType.value);
  const statusColumn = columns?.find((col) => col.field === 'status');

  if (statusColumn) {
    statusColumn.filters = statusFilters.value;
  }
  const internalColumn = columns?.find((col) => col.field === 'isInternal');

  if (internalColumn) {
    internalColumn.filters = internalFilters.value;
  }

  const effectTypelColumn = columns?.find((col) => col.field === 'effectType');

  if (effectTypelColumn) {
    effectTypelColumn.filters = effectTypeFilters.value;
  }

  gridApi.setGridOptions({
    columns,
  });
}

// 点击时动态取最新的表单值
const exportParams = async () => {
  const filters = await gridApi.grid?.getCheckedFilters();
  const filterParams = getFilterParams(filters);
  return {
    ...filterParams,
    bizType: currentType.value,
  };
};

onMounted(() => {
  loadDict();
});

/** 监听类型变化，清除筛选条件 */
watch(currentType, async () => {
  gridApi.grid?.clearFilter();
});
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
        <VxeTableExportButton :api="exportReceipt" :params="exportParams" />
      </template>
      <template #demandOrderNos="{ row }">
        <MultipleNosCell
          :items="row.demandOrderNos"
          :label="$t('report.receipt.demand')"
        />
      </template>
      <template #contractNos="{ row }">
        <MultipleNosCell
          :items="row.contractNos"
          :label="$t('report.receipt.contract')"
        />
      </template>
    </Grid>
  </Page>
</template>

<style lang="less" scoped></style>
