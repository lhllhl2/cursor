<script lang="ts" setup>
// import type { Recordable } from '@vben/types';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Checkbox, message, Select, Input } from 'ant-design-vue';
import dayjs from 'dayjs';

import {
  VxeTableEditingCell,
  VxeTableExpandTreeButton,
  VxeTableExportButton,
  VxeTableImportButton,
} from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportOrgs,
  getManageOrg,
  getOrgsPage,
  importOrgs,
  syncEhrManageOneRApiData,
  syncEhrManageRApiData,
  syncOrgs,
  updateOrgs,
} from '#/api';
import { PERMISSION_ENUM } from '#/enums';
import { useMultiRequest } from '#/hooks';
import { $t } from '#/locales';
import { yearOptions } from '#/utils';

import { useColumns } from './data';

const year = ref(dayjs().format('YYYY'));
const apiSyncLoading = ref<boolean>(false);
const orgsList = ref([]);
const orgNameMap = ref<Record<string, string>>({});

function buildOrgNameMap(list: any[]): Record<string, string> {
  if (!Array.isArray(list) || list.length === 0) {
    return {};
  }
  const map: Record<string, string> = {};
  const stack = [...list];
  const visited = new Set<any>();
  while (stack.length > 0) {
    const item = stack.pop();
    if (!item || visited.has(item)) {
      continue;
    }
    visited.add(item);
    if (item.orgCd) {
      map[item.orgCd] = item.orgNm || '';
    }
    if (Array.isArray(item.children) && item.children.length > 0) {
      stack.push(...item.children);
    }
  }
  return map;
}

function getOrgNameByCode(orgCd: string | null | undefined): string {
  if (!orgCd) {
    return '';
  }
  return orgNameMap.value[orgCd] || '';
}

const [pageService, manageOrgService, orgSyncService] = useMultiRequest([
  {
    request: getOrgsPage,
    config: {
      onSuccess: () => { },
    },
  },
  {
    request: getManageOrg,
    config: {
      onSuccess: (res: any) => {
        orgsList.value = res;
        orgNameMap.value = buildOrgNameMap(res);
        // res 是树形结构label->orgNm,value->orgCd，需要递归
        const processData = (items: any[]): any[] => {
          return items.map((item) => ({
            label: `${item.orgCd} ${item.orgNm}`,
            value: item.orgCd,
            children: item.children ? processData(item.children) : undefined,
          }));
        };
        const treeData = processData(res);
        gridApi.setGridOptions({
          columns: useColumns(treeData),
        });
      },
    },
  },
  {
    request: syncOrgs,
    config: {
      onSuccess: () => {
        message.success($t('common.syncSuccess'));
        gridApi.query();
      },
    },
  },
  {
    request: importOrgs,
    config: {
      onSuccess: (res: any) => {
        gridApi.query();
        message.success(res || $t('ui.actionMessage.operationSuccess'));
      },
    },
  },
  {
    request: updateOrgs,
    config: {
      onSuccess: (res: any) => {
        message.success(res || $t('ui.actionMessage.operationSuccess'));
      },
    },
  },
]);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    // columns: useColumns(manageOrgList.value),
    height: 'auto',
    keepSource: true,
    // filterConfig: {
    //   remote: true,
    // },
    editConfig: {
      trigger: 'dblclick',
      mode: 'cell',
      showStatus: true,
      beforeEditMethod(table: any) {
        const { row, column } = table;
        // 确保row.loading存在，避免访问undefined的属性
        if (row.loading?.[column.field]) {
          return false;
        }
        return true;
      },
    },
    scrollY: {
      enabled: true,
      gt: 0,
      oSize: 20,
    },
    pagerConfig: {
      enabled: false,
    },
    treeConfig: {
      parentField: 'ehrParCd',
      rowField: 'ehrCd',
      transform: true,
      expandAll: true,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          return await pageService
            ?.run({
              pageNo: 1,
              pageSize: 99_999,
              // ...queryParams.value,
              year: year.value,
            })
            .then((res) => {
              // 重要：树形结构转换（transform: true）需要完整数据才能正确建立父子关系
              // 必须一次性返回所有数据，不能分批，否则会破坏树形结构
              const list = res.list || [];
              if (list.length === 0) return [];

              // 为每行创建独立的loading和editError对象（避免共享引用导致的问题）
              for (const item of list) {
                if (!item.loading) {
                  item.loading = {
                    orgCd: false,
                    controlLevel: false,
                    bzLevel: false,
                    erpDepart: false,
                  };
                }
                if (!item.editError) {
                  item.editError = {
                    orgCd: null,
                    controlLevel: null,
                    bzLevel: null,
                    erpDepart: null,
                  };
                }
              }
              return list;
            });
        },
      },
    },
    actionWrapperClass: 'text-center',
  } as VxeTableGridOptions<any>,
  gridEvents: {
    async editClosed(table: any) {
      const { column, row, $grid } = table;

      if ($grid.isUpdateByRow(row)) {
        // 确保loading和editError存在（延迟初始化，减少初始处理时间）
        if (!row.loading) {
          row.loading = { orgCd: false, controlLevel: false, bzLevel: false, erpDepart: false };
        }
        if (!row.editError) {
          row.editError = { orgCd: null, controlLevel: null, bzLevel: null, erpDepart: null };
        }

        if (column.field === 'orgCd') {
          row.orgNm = getOrgNameByCode(row.orgCd) || row.orgNm || '';
        }

        row.loading[column.field] = true;
        row.controlLevel = row.controlLevel ? '1' : null;
        row.bzLevel = row.bzLevel ? '1' : null;
        const requestParams = { ...row };
        if (column.field === 'orgCd' && !requestParams.orgNm) {
          requestParams.orgNm = getOrgNameByCode(requestParams.orgCd) || '';
        }
        await updateOrgs({ key: column.field, params: requestParams })
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

async function loadManageOrg() {
  await manageOrgService?.run();
}

function onSync() {
  orgSyncService?.run();
}

/**
 * 接口数据更新：按顺序调用两个接口
 */
async function onApiSync() {
  try {
    apiSyncLoading.value = true;
    // 第一步：调用 synEhrManageOneRData
    await syncEhrManageOneRApiData();

    // 第二步：调用 syncEhrManageRData
    await syncEhrManageRApiData();

    // 完成后刷新数据
    message.success($t('common.syncSuccess'));
    // gridApi.query();
  } catch (error: any) {
    message.error(error.msg || $t('ui.actionMessage.operationFailed'));
  } finally {
    apiSyncLoading.value = false;
  }
}

onMounted(() => {
  loadManageOrg();
});
</script>
<template>
  <Page auto-content-height :description="$t('import.orgs.description')">
    <template #extra>
      <Button type="primary" @click="onApiSync" class="mr-2" :loading="apiSyncLoading"
        v-access="PERMISSION_ENUM.ImportOrgsApiSync">
        {{ $t('import.orgs.apiSync') }}
      </Button>
      <Button type="default" danger :loading="orgSyncService?.loading.value" @click="onSync">
        {{ $t('common.sync') }}
      </Button>
    </template>
    <Grid>
      <template #table-title>
        <IconifyIcon icon="carbon:calendar" class="year-icon" />
        <Select v-model:value="year" :options="yearOptions()" :bordered="false" class="year-select w-[90px]"
          @select="onYearChange" />
        <span class="text-primary flex items-center text-xs">
          <IconifyIcon icon="carbon:information-filled" class="ml-1 mr-1 inline-block size-4" />
          双击有
          <i class="vxe-table-icon-edit ml-1 mr-1 text-lg text-gray-500"></i>图标的列可编辑，编辑完成后点击空白处保存！
        </span>
      </template>
      <template #toolbar-tools>
        <VxeTableExpandTreeButton class="mr-2" :initial-expanded="true" :grid="gridApi.grid" />
        <VxeTableExportButton class="mr-2" :api="exportOrgs" v-access="PERMISSION_ENUM.ImportOrgsExport" />
        <VxeTableImportButton :api="importOrgs" :grid-api="gridApi" v-access="PERMISSION_ENUM.ImportOrgsImport" />
      </template>
      <template #orgCd="{ row, column }">
        <VxeTableEditingCell :loading="row.loading?.[column.field]" :error="row.editError?.[column.field]">
          <span :style="{ fontSize: '12px', color: 'rgb(9, 9, 11)' }">{{
            row.orgCd || '-'
            }}</span>
        </VxeTableEditingCell>
      </template>
      <template #yes="{ row, column }">
        <VxeTableEditingCell :loading="row.loading?.[column.field]" :error="row.editError?.[column.field]">
          <span class="text-primary font-bold">{{
            row[column.field] === '1' ? '✓' : '-'
            }}</span>
        </VxeTableEditingCell>
      </template>
      <template #text="{ row, column }">
        <VxeTableEditingCell :loading="row.loading?.[column.field]" :error="row.editError?.[column.field]">
          <span>{{ row[column.field] || '-' }}</span>
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
