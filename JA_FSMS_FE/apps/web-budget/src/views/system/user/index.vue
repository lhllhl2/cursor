<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

import { onMounted, ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportUserList,
  getUserGroupTree,
  getUserList,
  importUserList,
  resetUserPassword,
} from '#/api';
import { PERMISSION_ENUM } from '#/enums';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import UserGroupCascader from './modules/Cascader.vue';
import UserTransfer from './modules/copy.vue';

const { hasAccessByCodes } = useAccess();

const currentCopyRow = ref<null | SystemUserApi.SystemUser>(null);
const userGroupTreeOptions = ref<any[]>([]);
const hasLoadedTree = ref(false);

// 初始化用户组树
async function initUserGroupTree(userId?: string) {
  if (hasLoadedTree.value) {
    return;
  }
  const res = await getUserGroupTree(userId || '');
  if (res && Array.isArray(res)) {
    userGroupTreeOptions.value = res;
    hasLoadedTree.value = true;
  }
}

const [CopyUserModal, copyUserModalApi] = useVbenModal({
  title: '复制用户组',
  footer: false,
  onCancel() {
    copyUserModalApi.close();
    currentCopyRow.value = null;
  },
});

async function handleResetPassword(row: SystemUserApi.SystemUser) {
  try {
    await resetUserPassword({ userIds: [row.id as string] });
    message.success('重置成功');
  } catch (error: any) {
    console.error('重置密码失败:', error);
  }
}

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemUserApi.SystemUser>) {
  if (code === 'copy') {
    currentCopyRow.value = row;
    copyUserModalApi.setData(row).open();
  } else if (code === 'resetPassword') {
    handleResetPassword(row);
  }
}

function handleCopySuccess() {
  copyUserModalApi.close();
  currentCopyRow.value = null;
  gridApi.query();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
    showCollapseButton: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserList({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }).then(async (res: any) => {
            if (!hasLoadedTree.value && res.list && res.list.length > 0) {
              const firstUserId = res.list[0]?.id?.toString();
              if (firstUserId) {
                await initUserGroupTree(firstUserId);
              }
            }
            res.list = res.list.map((item: any) => {
              item.userGroup = ['1'];
              item.groupIds = Array.isArray(item.groupIds)
                ? item.groupIds.map(String)
                : [];
              return item;
            });
            return res;
          });
        },
      },
    },
    importConfig: hasAccessByCodes([PERMISSION_ENUM.SystemUserImport])
      ? {
          types: ['xlsx'],
          modes: ['insertTop'],
          remote: true,
          async importMethod(options: any) {
            await importUserList({ ...options }).then((res) => {
              gridApi.query();
              message.success(res || $t('common.importSuccess'));
            });
          },
        }
      : undefined,
  } as VxeTableGridOptions<SystemUserApi.SystemUser>,
});

onMounted(() => {
  initUserGroupTree();
});
</script>
<template>
  <Page auto-content-height>
    <Grid :table-title="$t('system.user.list')">
      <template #toolbar-tools>
        <VxeTableExportButton
          :api="exportUserList"
          v-access="PERMISSION_ENUM.SystemUserExport"
        />
      </template>
      <template #userGroup="{ row }">
        <UserGroupCascader
          :row="row"
          :tree-options="userGroupTreeOptions"
          @update:group-ids="(groupIds) => (row.groupIds = groupIds)"
        />
      </template>
    </Grid>

    <CopyUserModal class="w-[700px]">
      <div class="p-1">
        <UserTransfer
          :from-user-id="currentCopyRow?.id"
          @success="handleCopySuccess"
        />
      </div>
    </CopyUserModal>
  </Page>
</template>
