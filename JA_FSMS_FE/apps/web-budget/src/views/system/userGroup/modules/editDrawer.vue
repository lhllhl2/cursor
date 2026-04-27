<script lang="ts" setup>
import type { SystemUserGroupApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { editUserGroup } from '#/api';
import { $t } from '#/locales';

import { useEditFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemUserGroupApi.SystemUserGroup>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  schema: useEditFormSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: onSubmit,
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemUserGroupApi.SystemUserGroup>();
      if (data) {
        formData.value = data;
        formApi.setValues({
          name: data.name,
          remark: data.remark,
        });
      }
    }
  },
});

async function onSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) return;
  const values = await formApi.getValues();
  drawerApi.lock();
  try {
    await editUserGroup({
      id: formData.value?.id,
      name: values.name,
      remark: values.remark,
    });
    message.success($t('ui.actionMessage.operationSuccess'));
    drawerApi.close();
    emits('success');
  } finally {
    drawerApi.unlock();
  }
}

const getDrawerTitle = computed(() =>
  $t('ui.actionTitle.edit', [$t('system.userGroup.name')]),
);
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <Form />
  </Drawer>
</template>
