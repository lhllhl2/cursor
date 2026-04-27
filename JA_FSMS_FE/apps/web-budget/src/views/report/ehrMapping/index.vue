<script setup lang="ts">
import type { Recordable } from '@vben/types';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportEhrMapping, getEhrMappingListApi } from '#/api';
import { $t } from '#/locales';

import { useColumns } from './data';

const queryParams = ref<Recordable<any>>({});

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: false,
    filterConfig: {
      remote: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getEhrMappingListApi({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...queryParams.value,
          });
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
/** 导出参数 */
const exportParams = () => {
  return {
    ...queryParams.value,
  };
};
</script>

<template>
  <Page auto-content-height>
    <Grid :table-title="$t('report.ehrMapping.list')">
      <template #toolbar-tools>
        <VxeTableExportButton :api="exportEhrMapping" :params="exportParams" />
      </template>
    </Grid>
  </Page>
</template>
