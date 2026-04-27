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
        help: '当前密码强度：强',
      },
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'securityPhone',
      label: '密保手机',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '已绑定手机：138****8293',
      },
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'securityQuestion',
      label: '密保问题',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '未设置密保问题，密保问题可有效保护账户安全',
      },
    },
    {
      component: 'Switch',
      defaultValue: true,
      fieldName: 'securityEmail',
      label: '备用邮箱',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '已绑定邮箱：ant***sign.com',
      },
    },
    {
      component: 'Switch',
      defaultValue: false,
      fieldName: 'securityMfa',
      label: 'MFA 设备',
      componentProps: {
        checkedChildren: '开',
        unCheckedChildren: '关',
      },
      formItemProps: {
        help: '未绑定 MFA 设备，绑定后，可以进行二次确认',
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
