<script setup lang="ts">
import { computed } from 'vue';

import { Tooltip, Tag } from 'ant-design-vue';

const props = withDefaults(
  defineProps<{
    /** 单号列表，支持数组或逗号分隔字符串 */
    items?: string[] | string;
    /** 单位标签，如「需求单」「合同单」 */
    label: string;
  }>(),
  {
    items: () => [],
  },
);

const display = computed(() => {
  const list = Array.isArray(props.items)
    ? props.items
    : props.items
      ? String(props.items).split(',')
      : [];
  if (list.length === 0) return { text: '-', tooltip: '' };
  if (list.length === 1) return { text: list[0], tooltip: '' };
  return {
    text: `${list.length}个${props.label}`,
    tooltip: list.join(', '),
  };
});
</script>

<template>
  <template v-if="items.length === 0">
    <span>-</span>
  </template>
  <Tooltip v-else-if="display.tooltip" :title="display.tooltip">
    <span class="cursor-pointer text-primary">{{ display.text }}</span>
  </Tooltip>
  <Tag v-else>{{ display.text }}</Tag>
</template>
