<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';

import { ref } from 'vue';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';
import { $t } from '@vben/locales';

import { Button, message } from 'ant-design-vue';

import { VxeTableExpandTreeButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMenu, getMenuList, SystemMenuApi } from '#/api';
import { PERMISSION_ENUM } from '#/enums';

import { useColumns } from './data';
import BatchLangs from './modules/batchLangs.vue';
import Form from './modules/form.vue';
import Langs from './modules/langs.vue';
import { getParentMenus } from './utils';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [LangsModal, langsModalApi] = useVbenModal({
  connectedComponent: Langs,
  destroyOnClose: true,
});

const [BatchLangsModal, batchLangsModalApi] = useVbenModal({
  connectedComponent: BatchLangs,
  destroyOnClose: true,
});

const dataSource = ref<SystemMenuApi.SystemMenu[]>([]);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params) => {
          return await getMenuList().then((res) => {
            dataSource.value = res;
            return res;
          });
        },
      },
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: { code: 'query' },
      zoom: true,
    },
    treeConfig: {
      parentField: 'pid',
      rowField: 'id',
      transform: false,
    },
  } as VxeTableGridOptions,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemMenuApi.SystemMenu>) {
  switch (code) {
    case 'append': {
      onAppend(row);
      break;
    }
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'edit': {
      onEdit(row);
      break;
    }
    case 'langs': {
      onLangs(row);
      break;
    }
    default: {
      break;
    }
  }
}

function onRefresh() {
  gridApi.query();
}
function onEdit(row: SystemMenuApi.SystemMenu) {
  formDrawerApi.setData(row).open();
}
function onCreate() {
  formDrawerApi.setData({}).open();
}
function onAppend(row: SystemMenuApi.SystemMenu) {
  formDrawerApi.setData({ pid: row.id }).open();
}

function onLangs(row: SystemMenuApi.SystemMenu) {
  const parentMenus = getParentMenus(row.id, dataSource.value);
  const menuPath = parentMenus;

  langsModalApi
    .setData({
      id: row.id,
      title: row.meta?.title,
      menuPath, // 传递父级菜单数组
    })
    .open();
}

function onDelete(row: SystemMenuApi.SystemMenu) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteMenu(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.name]),
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onBatchLangs() {
  batchLangsModalApi.open();
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <LangsModal @success="onRefresh" />
    <BatchLangsModal @success="onRefresh" />
    <Grid>
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.menu.name')]) }}
        </Button>
        <Button
          class="ml-2"
          @click="onBatchLangs"
          v-access="PERMISSION_ENUM.SystemMenuI18NCONFIG"
        >
          <IconifyIcon icon="carbon:language" />
          {{ $t('system.menu.i18nConfig') }}
        </Button>
        <VxeTableExpandTreeButton class="ml-2" :grid="gridApi.grid" />
      </template>
      <!-- <template #headerTitle="{ column }">
        <div class="mr-1 pl-1">
          {{ column?.title }}
          <IconifyIcon icon="carbon:overflow-menu-vertical" class="inline-block size-5"/>
        </div>
      </template> -->
      <template #title="{ row }">
        <div class="flex w-full items-center gap-1">
          <div class="size-5 flex-shrink-0">
            <IconifyIcon
              v-if="row.type === 'button'"
              icon="carbon:security"
              class="size-full"
            />
            <IconifyIcon
              v-else-if="row.meta?.icon"
              :icon="row.meta?.icon || 'carbon:circle-dash'"
              class="size-full"
            />
          </div>
          <span class="flex-auto">{{ $t(row.meta?.title) }}</span>
          <div class="items-center justify-end"></div>
        </div>
      </template>
    </Grid>
  </Page>
</template>
<style lang="scss" scoped>
.menu-badge {
  top: 50%;
  right: 0;
  transform: translateY(-50%);

  & > :deep(div) {
    padding-top: 0;
    padding-bottom: 0;
  }
}
</style>
