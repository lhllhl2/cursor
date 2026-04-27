<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';

import { computed, reactive } from 'vue';

import { useVbenForm } from '#/adapter/form';

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'accountPassword',
      label: '账户密码',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '其他用户的消息将以站内信的形式通知',
      },
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'systemMessage',
      label: '系统消息',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '系统消息将以站内信的形式通知',
      },
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'todoTask',
      label: '待办任务',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '待办任务将以站内信的形式通知',
      },
    },
  ];
});

const [Form] = useVbenForm(
  reactive({
    commonConfig: {
      labelWidth: 120,
    },
    schema: computed(() => formSchema.value),
    showDefaultActions: false,
  }),
);
</script>
<template>
  <Form />
</template>
