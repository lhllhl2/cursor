<script lang="ts" setup>
import type { SystemUserGroupApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message, Steps } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { addUserGroup, groupUserRSave } from '#/api';
import { $t } from '#/locales';

import { stepItems, useFirstFormSchema } from '../data';
import UserTransfer from './userTransfer.vue';

const emit = defineEmits<{
  success: [];
}>();

const formData = ref<SystemUserGroupApi.SystemUserGroup>({});
const isEdit = computed(() => {
  return formData.value?.id;
});
const currentTab = ref(0);
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.userGroup.name')])
    : $t('ui.actionTitle.create', [$t('system.userGroup.name')]);
});

const [Modal, modalApi] = useVbenModal({
  onCancel() {
    modalApi.close();
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<SystemUserGroupApi.SystemUserGroup>();
      if (data) {
        formData.value = data;
        firstFormApi.setValues(formData.value);
      }
    }
  },
  footer: false,
});
const [FirstForm, firstFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  handleSubmit: onFirstSubmit,
  layout: 'horizontal',
  submitButtonOptions: {
    content: $t('common.next'),
  },
  schema: useFirstFormSchema(),
  wrapperClass: 'grid-cols-1 md:grid-cols-1 lg:grid-cols-1',
});

async function onFirstSubmit() {
  const { valid } = await firstFormApi.validate();
  if (valid) {
    currentTab.value = 1;
  }
}

function onSecondReset() {
  currentTab.value = 0;
}

async function onSecondSubmit(ids: string[]) {
  if (isEdit.value) {
    await groupUserRSave({
      groupId: formData.value.id,
      userIds: ids,
    });
  } else {
    const values = await firstFormApi.getValues();
    const params = {
      ...values,
      userIds: ids,
    };
    await addUserGroup(params);
  }
  message.success($t('ui.actionMessage.operationSuccess'));
  modalApi.close();
  emit('success');
}
</script>
<template>
  <Modal :title="getTitle" class="w-[700px]">
    <div class="p-5" v-if="!isEdit">
      <Steps :current="currentTab" class="steps" :items="stepItems" />
      <div class="p-5">
        <FirstForm v-show="currentTab === 0" />
        <UserTransfer
          v-show="currentTab === 1"
          :cancel-props="{ text: $t('common.prev'), show: true }"
          @submit="onSecondSubmit"
          @cancel="onSecondReset"
        />
      </div>
    </div>
    <div v-else class="p-1">
      <UserTransfer
        :group-id="formData.id"
        :cancel-props="{ show: false }"
        @submit="onSecondSubmit"
      />
    </div>
  </Modal>
</template>
