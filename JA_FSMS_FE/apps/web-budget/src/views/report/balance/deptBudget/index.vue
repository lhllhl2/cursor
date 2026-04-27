<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportDeptBudgetBalance, queryDeptBudgetBalance } from '#/api';

import { useColumns } from './data';

const queryParams = ref<Recordable<any>>({});

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    filterConfig: {
      remote: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const response = await queryDeptBudgetBalance({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...queryParams.value,
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
      // 映射字段名
      if (params.subjectCode) {
        params.accountSubjectCode = params.subjectCode;
        delete params.subjectCode;
      }
      if (params.subjectName) {
        params.accountSubjectName = params.subjectName;
        delete params.subjectName;
      }
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
    <Grid>
      <template #toolbar-tools>
        <VxeTableExportButton
          :api="exportDeptBudgetBalance"
          :params="exportParams"
        />
      </template>
    </Grid>
  </Page>
</template>

<style lang="less" scoped></style>
