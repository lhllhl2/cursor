<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';

import { computed, reactive } from 'vue';

import { message } from 'ant-design-vue';

import { useVbenForm, z } from '#/adapter/form';

// const profilePasswordSettingRef = ref();

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      fieldName: 'oldPassword',
      label: '旧密码',
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: '请输入旧密码',
      },
    },
    {
      fieldName: 'newPassword',
      label: '新密码',
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: '请输入新密码',
      },
    },
    {
      fieldName: 'confirmPassword',
      label: '确认密码',
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: '请再次输入新密码',
      },
      dependencies: {
        rules(values) {
          const { newPassword } = values;
          return z
            .string({ required_error: '请再次输入新密码' })
            .min(1, { message: '请再次输入新密码' })
            .refine((value) => value === newPassword, {
              message: '两次输入的密码不一致',
            });
        },
        triggerFields: ['newPassword'],
      },
    },
  ];
});

const [Form, formApi] = useVbenForm(
  reactive({
    commonConfig: {
      labelWidth: 120,
    },
    schema: computed(() => formSchema.value),
    showDefaultActions: true,
    handleSubmit: async () => {
      const { valid } = await formApi.validate();
      if (valid) {
        message.success('密码修改成功');
      }
    },
  }),
);

defineExpose({
  getFormApi: () => formApi,
});
</script>
<template>
  <div class="w-1/3">
    <Form />
  </div>
</template>
