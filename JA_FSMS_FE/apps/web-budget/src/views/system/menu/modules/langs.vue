<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { h, reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { SUPPORT_LANGUAGES } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Button, Input, message, notification } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getLangs, updateLangs } from '#/api/system/langs';
import { $t } from '#/locales';
import { deepClone } from '#/utils/common';
// import translations from './translations.json';

interface RowType {
  title: string;
  [key: string]: number | string;
}
const dataSource = ref<RowType[]>([]);
const nameFilterRender = reactive({
  name: 'VxeInput',
});
const tableTitle = ref<string>('');
const loading = ref(false);
const isSave = ref(false);
const pagination = {
  currentPage: 1,
  pageSize: 10,
  total: 0,
};

const [Modal, modalApi] = useVbenModal({
  onClosed() {
    isSave.value &&
      notification.open({
        message: '系统提示',
        description: '系统检查到，您更新了多语言匹配，是否需要重新加载系统！',
        icon: () =>
          h(IconifyIcon, {
            icon: 'ant-design:exclamation-circle-filled',
            class: 'text-warning',
          }),
        btn: () =>
          h(
            Button,
            {
              type: 'primary',
              size: 'small',
              danger: true,
              onClick: () => {
                window.location.reload();
              },
            },
            { default: () => '加载' },
          ),
        key: '1',
        onClose: () => {},
      });
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      isSave.value = false;
      fetch();
      handleTitle();
      getMenuPath();
    }
  },
  // title: $t('system.menu.langs'),
  footer: false,
});

const gridOptions: VxeGridProps<RowType> = {
  loading: loading.value,
  columns: [
    {
      field: 'title',
      title: $t('system.menu.langsField'),
      editRender: {},
      slots: { edit: 'titleEdit' },
      filters: [{ data: '' }],
      filterRender: nameFilterRender,
    },
    ...SUPPORT_LANGUAGES.map((item) => ({
      editRender: { name: 'input' },
      field: item.value,
      title: item.label,
    })),
    { slots: { default: 'action' }, title: $t('system.menu.operation') },
  ],
  showOverflow: true,
  keepSource: true,
  // filterConfig: {
  //   confirmButtonText: $t('common.search'),
  //   resetButtonText: $t('common.reset'),
  // },
  editConfig: {
    trigger: 'dblclick',
    mode: 'row',
    showStatus: true,
  },
  editRules: {
    title: [
      {
        required: true,
        message: $t('ui.formRules.required', [$t('system.menu.langsField')]),
      },
    ],
  },
  pagerConfig: pagination,
  toolbarConfig: {
    custom: false,
    refresh: false,
    zoom: false,
  },
  // data: dataSource.value,
};

const gridEvents = {
  pageChange({
    pageSize,
    currentPage,
  }: {
    currentPage: number;
    pageSize: number;
  }) {
    gridApi.setGridOptions({
      pagerConfig: {
        currentPage,
        pageSize,
      },
    });
    onPageData(pageSize, currentPage);
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions, gridEvents });

async function fetch() {
  loading.value = true;
  const data = modalApi.getData();
  dataSource.value = data.id ? await getLangs({ menuId: data.id }) : [];
  const allList = deepClone(dataSource.value);
  const total = dataSource.value.length;
  gridApi.setGridOptions({
    pagerConfig: {
      total,
      currentPage: 1,
      pageSize: 10,
    },
    data: allList.slice(
      (pagination.currentPage - 1) * pagination.pageSize,
      pagination.currentPage * pagination.pageSize,
    ),
  });
  loading.value = false;
}

async function onPageData(pageSize: number, currentPage: number) {
  setTimeout(() => {
    const allList = deepClone(dataSource.value);
    gridApi.setGridOptions({
      data: allList.slice((currentPage - 1) * pageSize, currentPage * pageSize),
    });
  }, 100);
}

async function onAdd() {
  const record = {
    title: ``,
  };
  const { row: newRow } = await gridApi.grid.insert(record);
  gridApi.grid.setEditRow(newRow);
}

function deleteRowEvent(row: RowType, status: boolean) {
  gridApi.grid.setPendingRow(row, status);
}

function isPendingRow(row: RowType) {
  return gridApi.grid.isPendingByRow(row);
}

function onRevert() {
  gridApi.grid.revertData();
}

async function onSave() {
  const errMap = await gridApi.grid.validate(true);
  if (errMap) {
    return;
  }
  const _ds = deepClone(dataSource.value);
  const insertRecords = gridApi.grid.getInsertRecords();
  const pendingRecords = gridApi.grid.getPendingRecords();
  const updateRecords = gridApi.grid.getUpdateRecords();
  if (insertRecords.length > 0) {
    // 新增
    _ds.unshift(...insertRecords);
  }
  if (pendingRecords.length > 0) {
    // 删除
    pendingRecords.forEach((item) => {
      const index = _ds.findIndex((i) => i.title === item.title);
      _ds.splice(index, 1);
    });
  }
  if (updateRecords.length > 0) {
    // 编辑
    updateRecords.forEach((item) => {
      const index = _ds.findIndex((i) => i.title === item.title);
      _ds[index] = item;
    });
  }
  const jsonData: Record<string, Record<string, number | string>> = {};
  _ds.forEach((item) => {
    jsonData[item.title] = {
      'zh-CN': item['zh-CN'] || '',
      'en-US': item['en-US'] || '',
    };
  });
  const params = {
    menuId: modalApi.getData().id,
    jsonData,
  };

  try {
    // const _params={
    //   menuId:translations.menuId,
    //   jsonData:translations.jsonData,
    // }
    await updateLangs(params);
    await fetch();
    setTimeout(() => {
      gridApi.grid.revertData();
    }, 100);
    isSave.value = true;
    message.success($t('ui.actionMessage.operationSuccess'));
  } catch {
    message.error($t('ui.actionMessage.operationFailed'));
  }
}

function handleTitle() {
  const title = modalApi.getData().title;
  modalApi.setState({ title: `${$t('system.menu.langs')}-${$t(title)}` });
}

function getMenuPath() {
  const menuPath = modalApi.getData().menuPath;
  const pathNames = menuPath.map((menu: any) => menu.name);
  tableTitle.value = `${$t('system.menu.langPath')} : ${pathNames.join('.')}`;
}
</script>
<template>
  <Modal class="min-h-[500px] w-full max-w-[800px]">
    <Grid :table-title="tableTitle">
      <template #toolbar-tools>
        <Button type="primary" @click="onAdd">
          {{ $t('ui.actionTitle.create') }}
        </Button>
        <Button class="ml-2" @click="onRevert">
          {{ $t('common.restore') }}
        </Button>
        <Button class="ml-2" type="primary" @click="onSave">
          {{ $t('common.save') }}
        </Button>
      </template>
      <template #action="{ row }">
        <template v-if="!isPendingRow(row)">
          <Button type="link" @click="deleteRowEvent(row, true)">
            {{ $t('ui.actionTitle.delete') }}
          </Button>
        </template>
        <template v-else>
          <Button type="link" @click="deleteRowEvent(row, false)">
            {{ $t('ui.actionTitle.cancel') }}
          </Button>
        </template>
      </template>
      <template #titleEdit="{ row }">
        <Input v-model:value="row.title" />
      </template>
    </Grid>
  </Modal>
</template>
<style scoped lang="less">
:deep(.vxe-table--filter-wrapper) {
  padding: 8px;

  .vxe-input {
    border: none;
  }
}
</style>
