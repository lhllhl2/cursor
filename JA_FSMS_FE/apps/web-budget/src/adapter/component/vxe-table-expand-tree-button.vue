<script lang="ts" setup>
import type { Ref } from 'vue';

import { computed, nextTick, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { $t } from '#/locales';
import { isEmptyObject } from '#/utils';

interface TreeGridInstance {
  setAllTreeExpand: (expanded: boolean) => Promise<void> | void;
}

interface TreeExpandButtonProps {
  grid?: null | TreeGridInstance;
  initialExpanded?: boolean;
  iconExpand?: string;
  iconCollapse?: string;
  titleExpandKey?: string;
  titleCollapseKey?: string;
  successExpandKey?: string;
  successCollapseKey?: string;
  errorMessageKey?: string;
  loading?: null | Ref<boolean>;
}

const props = withDefaults(defineProps<TreeExpandButtonProps>(), {
  grid: null,
  initialExpanded: false,
  iconExpand: 'carbon:intent-request-scale-out',
  iconCollapse: 'carbon:intent-request-scale-in',
  titleExpandKey: 'common.expandAll',
  titleCollapseKey: 'common.collapseAll',
  successExpandKey: '',
  successCollapseKey: '',
  errorMessageKey: '',
  loading: null,
});

const emits = defineEmits<{
  (event: 'click'): void;
  (event: 'toggle', expanded: boolean): void;
  (event: 'success'): void;
  (event: 'error', error: unknown): void;
}>();

const internalLoading = ref(false);
const isExpanded = ref(props.initialExpanded);

const buttonLoading = computed(
  () => props.loading?.value ?? internalLoading.value,
);

const isDisabled = computed(() => !props.grid);

const buttonTitle = computed(() =>
  $t(isExpanded.value ? props.titleCollapseKey : props.titleExpandKey),
);

const currentIcon = computed(() =>
  isExpanded.value ? props.iconCollapse : props.iconExpand,
);

watch(
  () => props.initialExpanded,
  (value) => {
    if (typeof value === 'boolean') {
      isExpanded.value = value;
    }
  },
);

watch(
  () => props.grid,
  (newGrid) => {
    if (newGrid && !isEmptyObject(newGrid as Record<string, any>)) {
      isExpanded.value = props.initialExpanded;
      if (typeof isExpanded.value === 'boolean') {
        nextTick(() => {
          Promise.resolve(newGrid.setAllTreeExpand(isExpanded.value));
        });
      }
    }
  },
  { immediate: true },
);

async function onClick(): Promise<void> {
  if (buttonLoading.value || !props.grid) {
    return;
  }
  internalLoading.value = true;
  try {
    const targetExpand = !isExpanded.value;
    await Promise.resolve(props.grid.setAllTreeExpand(targetExpand));
    if (targetExpand && props.successExpandKey) {
      message.success($t(props.successExpandKey));
    } else if (!targetExpand && props.successCollapseKey) {
      message.success($t(props.successCollapseKey));
    }
    isExpanded.value = targetExpand;
    emits('toggle', isExpanded.value);
    emits('success');
    emits('click');
  } catch (error) {
    if (props.errorMessageKey) {
      message.error($t(props.errorMessageKey));
    }
    emits('error', error);
  } finally {
    internalLoading.value = false;
  }
}

function syncExpandState() {
  const grid = props.grid;
  if (grid && typeof isExpanded.value === 'boolean') {
    nextTick(() => {
      Promise.resolve(grid.setAllTreeExpand(isExpanded.value));
    });
  }
}

defineExpose({
  syncExpandState,
  isExpanded,
});
</script>
<template>
  <Button
    type="default"
    shape="circle"
    class="tree-expand-btn"
    :title="buttonTitle"
    :loading="buttonLoading"
    :disabled="isDisabled"
    @click="onClick"
  >
    <IconifyIcon
      v-if="!buttonLoading"
      class="tree-expand-btn__icon"
      :icon="currentIcon"
    />
  </Button>
</template>

<style lang="less" scoped>
.tree-expand-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  width: 30px;
}

.tree-expand-btn__icon {
  font-size: 16px;
}
</style>
