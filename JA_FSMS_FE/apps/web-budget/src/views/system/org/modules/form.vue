<script lang="ts" setup>
import type { SystemOrgApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { Badge, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  // bindOrgTheme,
  // getOrgThemeList,
  getOrgUserGroup,
  orgBatchOperate,
} from '#/api';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const dataSource = ref<SystemOrgApi.SystemOrg | SystemOrgApi.SystemOrg[]>();
const formData = ref<SystemOrgApi.SystemOrg>();

const isBatch = computed(() => {
  return Array.isArray(dataSource.value) && dataSource.value.length > 0;
});

const getDrawerTitle = computed(() => {
  return isBatch.value
    ? $t('common.batch', [$t('system.org.auth')])
    : $t('system.org.auth');
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  // schema: useFormSchema(),
  showDefaultActions: false,
});
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const values = await formApi.getValues();
    const params = {
      organizationIds: [dataSource.value.id],
      userGroupIds: values.userGroupIds,
    };
    if (isBatch.value) {
      params.organizationIds = dataSource.value.map((item: any) => item.id);
      params.importMode = values.importMode;
    }
    drawerApi.lock();

    orgBatchOperate(params)
      .then(() => {
        message.success($t('ui.actionMessage.operationSuccess'));
        emits('success');
        drawerApi.close();
      })
      .catch(() => {
        drawerApi.unlock();
      });
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      dataSource.value = drawerApi.getData<SystemOrgApi.SystemOrg[]>();
      formApi.setState({
        schema: useFormSchema(isBatch.value),
      });
      formApi.resetForm();
      if (!Array.isArray(dataSource.value)) {
        await initData(dataSource.value);
      }
    }
  },
});

async function initData(data: SystemOrgApi.SystemOrg) {
  formData.value = data;
  const userGroups = await getOrgUserGroup({ orgId: data.id });

  if (userGroups.length > 0) {
    data.userGroupIds = userGroups;
  }
  formApi.setValues(data);
}
</script>
<template>
  <Drawer :title="getDrawerTitle">
    <div class="orgs">
      <div class="orgs-title" v-if="isBatch">
        已选（{{ dataSource?.length }}）个
      </div>
      <div class="grid grid-cols-2 gap-2">
        <template v-if="isBatch">
          <Badge
            v-for="value in dataSource"
            status="success"
            :key="value.id"
            :text="value.name"
          />
        </template>
        <Badge v-else status="success" :text="dataSource.name" />
      </div>
    </div>
    <Form />
  </Drawer>
</template>
<style lang="css" scoped>
.orgs {
  max-height: 300px;
  padding: 16px;
  overflow-y: auto;
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 6px;

  .orgs-title {
    margin-bottom: 10px;
    font-weight: 500;
    color: #262626;
  }
}
</style>
