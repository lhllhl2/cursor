<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { getFilterParams, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getApiLogPageApi } from '#/api';
import { $t } from '#/locales';

import { useColumns } from './data';

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(),
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
          const response = await getApiLogPageApi({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...filterParams,
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
</script>

<template>
  <Page auto-content-height>
    <Grid :table-title="$t('report.apiLog.list')">
      <template #status="{ row }">
        <span
          :class="row.status === 'SUCCESS' ? 'text-green-500' : 'text-red-500'"
        >
          {{ row.status }}
        </span>
      </template>
    </Grid>
  </Page>
</template>

<style lang="scss" scoped></style>
