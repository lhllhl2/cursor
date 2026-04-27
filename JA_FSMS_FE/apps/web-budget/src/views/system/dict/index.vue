<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api/';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { $te } from '@vben/locales';

import { Button, message, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDict, getDictList } from '#/api';
import { $t } from '#/locales';

import Langs from '../menu/modules/langs.vue';
import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [LangsModal, langsModalApi] = useVbenModal({
  connectedComponent: Langs,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getDictList({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
  } as VxeTableGridOptions<SystemDictApi.SystemDict>,
});

function onActionClick(e: OnActionClickParams<SystemDictApi.SystemDict>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
  }
}

function onEdit(row: SystemDictApi.SystemDict) {
  formDrawerApi.setData(row).open();
}

function onDelete(row: SystemDictApi.SystemDict) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.title]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteDict(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.title]),
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

function onLangs() {
  const menuPath = [
    {
      id: 1,
      name: 'system',
    },
    {
      id: 2,
      name: 'dict',
    },
  ];
  langsModalApi
    .setData({
      id: '18645767266766848',
      title: $t('system.dict.dictmanage'),
      menuPath, // 传递父级菜单数组
    })
    .open();
}

// 获取选项的显示文本（优先显示中文翻译，如果没有则显示英文关键字）
function getOptionDisplayText(fieldLabel: string): string {
  if (!fieldLabel) return '';

  const englishKeyword = fieldLabel.replace('system.dict.', '');
  const translation = $te(`system.dict.${englishKeyword}`)
    ? $t(`system.dict.${englishKeyword}`)
    : '';

  return translation || englishKeyword;
}
</script>
<template>
  <Page auto-content-height>
    <LangsModal @success="onRefresh" />
    <FormDrawer @success="onRefresh" />
    <Grid :table-title="$t('system.dict.list')">
      <template #toolbar-tools>
        <Button type="primary" class="mr-2" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.dict.dict')]) }}
        </Button>
        <Button type="primary" @click="onLangs">
          {{ $t('system.dict.langs') }}
        </Button>
      </template>
      <template #title="{ row }">
        <div class="flex w-full items-center gap-1">
          <span class="flex-auto">{{ $t(row?.title) }}</span>
          <div class="items-center justify-end"></div>
        </div>
      </template>
      <template #options="{ row }">
        <Tag v-for="item in row.labelList" :key="item.fieldKey">
          {{ getOptionDisplayText(item.fieldLabel) }}
        </Tag>
      </template>
    </Grid>
  </Page>
</template>
