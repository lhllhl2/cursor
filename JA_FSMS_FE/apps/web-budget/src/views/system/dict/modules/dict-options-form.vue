<script setup lang="ts">
import type { SystemDictApi } from '#/api';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $te } from '@vben/locales';

import { Button, Input } from 'ant-design-vue';

import { $t } from '#/locales';

import { DICT_PREFIX } from '../data';

const props = defineProps<{
  modelValue: SystemDictApi.SystemDict['options'];
}>();

const emit = defineEmits(['update:modelValue']);

const options = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

const onAdd = () => {
  const newOptions = [...(options.value || [])];
  newOptions.push({
    fieldKey: '',
    fieldLabel: '',
  });
  options.value = newOptions;
};

const onRemove = (index: number) => {
  options.value.splice(index, 1);
};
const getFieldLabelSuffix = (fieldLabel: string) => {
  if (!fieldLabel) return '';
  return $te(DICT_PREFIX + fieldLabel) ? $t(DICT_PREFIX + fieldLabel) : '';
};
</script>

<template>
  <div class="dict-options-form">
    <div
      v-for="(option, index) in options"
      :key="index"
      class="mb-2 flex items-center gap-2"
    >
      <Input
        class="input-field-key"
        v-model:value="option.fieldKey"
        :placeholder="$t('system.dict.validateFieldKey')"
      />
      <Input
        :addon-before="DICT_PREFIX"
        :addon-after="getFieldLabelSuffix(option.fieldLabel)"
        class="flex-1"
        :placeholder="$t('system.dict.vaildateFieldLabel')"
        v-model:value="option.fieldLabel"
      />
      <IconifyIcon
        icon="ant-design:delete-outlined"
        class="cursor-pointer text-red-500"
        @click="onRemove(index)"
      />
    </div>

    <Button type="dashed" block @click="onAdd">
      <IconifyIcon icon="ant-design:plus-circle-outlined" />
      {{ $t('ui.actionTitle.create', [$t('system.dict.options')]) }}
    </Button>
  </div>
</template>

<style lang="less" scoped>
.dict-options-form {
  width: 100%;
  padding: 16px;
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  .input-field-key {
    flex: 0 0 120px;
    min-width: 120px;
  }
}
</style>
