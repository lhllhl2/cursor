<script lang="ts" setup>
import type { SystemDictApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createDict, updateDict } from '#/api';
import { $t } from '#/locales';

import { DICT_PREFIX, useFormSchema } from '../data';
import DictOptionsForm from './dict-options-form.vue';

const emits = defineEmits(['success']);

const formData = ref<SystemDictApi.SystemDict>();
const titleSuffix = ref<string>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full',
    },
  },
  schema: useFormSchema(titleSuffix),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: onSubmit,
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemDictApi.SystemDict>();

      if (data?.id) {
        // 编辑模式，设置表单数据
        // 处理回显数据，去掉 system.dict. 前缀
        // 优先使用 labelList，如果没有则使用 options
        const optionsData = data.labelList || data.options || [];

        const processedData = {
          ...data,
          title: data.title?.startsWith(DICT_PREFIX)
            ? data.title.replace(DICT_PREFIX, '')
            : data.title,
          options: optionsData.map((option: any) => ({
            ...option,
            fieldLabel: option.fieldLabel?.startsWith(DICT_PREFIX)
              ? option.fieldLabel.replace(DICT_PREFIX, '')
              : option.fieldLabel,
          })),
        };
        titleSuffix.value = $t(data.title);
        formData.value = {
          ...processedData,
          options:
            processedData.options.length > 0 ? processedData.options : [],
        };
        formApi.setValues(formData.value);
      } else {
        // 新增模式，重置表单
        formData.value = {
          code: '',
          title: '',
          options: [],
        };
        titleSuffix.value = '';
      }
    }
  },
});

async function onSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) return;
  const values = await formApi.getValues();
  drawerApi.lock();
  let processedOptions = [];
  if (values.options.length > 0) {
    // 处理保存数据，为 title 和 fieldLabel 添加 system.dict. 前缀，只保存英文关键字
    processedOptions = values.options.map((option: any) => ({
      ...option,
      fieldLabel: option.fieldLabel.startsWith(DICT_PREFIX)
        ? option.fieldLabel
        : `${DICT_PREFIX}${option.fieldLabel}`,
    }));
  }
  const params = {
    ...values,
    title: values.title.startsWith(DICT_PREFIX)
      ? values.title
      : `${DICT_PREFIX}${values.title}`,
    labelList: processedOptions,
  };

  try {
    await (formData.value?.id
      ? updateDict({
          ...params,
          id: formData.value.id,
        })
      : createDict(params));

    drawerApi.close();
    message.success($t('ui.actionMessage.operationSuccess'));
    emits('success');
  } finally {
    drawerApi.unlock();
  }
}

const getDrawerTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.dict.dict')])
    : $t('ui.actionTitle.create', [$t('system.dict.dict')]),
);
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <Form>
      <template #options="slotProps">
        <DictOptionsForm v-bind="slotProps" />
      </template>
    </Form>
  </Drawer>
</template>

<style lang="scss" scoped>
// 字典管理输入框样式
.dict-title-input {
  :deep(.ant-input) {
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    white-space: nowrap !important;
  }

  :deep(.ant-input-group-addon) {
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    white-space: nowrap !important;
  }

  // system.dict. 样式
  :deep(.ant-input-group-addon:first-child) {
    overflow: visible !important; // 确保文本不被截断
    white-space: nowrap !important; // 防止换行
    background-color: #fafafa !important; // 与选项框背景色相同
    border-color: #d9d9d9 !important;
  }
}
</style>
