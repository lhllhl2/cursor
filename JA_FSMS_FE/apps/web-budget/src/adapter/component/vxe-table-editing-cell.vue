<!-- 编辑单元格，支持编辑状态和错误状态 -->
<script lang="ts" setup>
import { IconifyIcon } from '@vben/icons';

import { Spin, Tooltip } from 'ant-design-vue';

const props = defineProps<{
  error: null | string;
  loading: boolean;
}>();
</script>
<template>
  <Spin :spinning="loading">
    <slot name="default"></slot>
    <Tooltip :title="error" color="oklch(70.4% 0.191 22.216)">
      <IconifyIcon
        v-if="error"
        icon="carbon:warning-filled"
        class="ml-1 inline-block size-4 cursor-pointer text-red-500"
      />
    </Tooltip>
  </Spin>
</template>
