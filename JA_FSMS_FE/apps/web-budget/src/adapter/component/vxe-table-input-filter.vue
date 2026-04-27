<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { Button, Input, Radio, RadioGroup } from 'ant-design-vue';

const props = defineProps({
  renderOpts: Object,
  renderParams: Object,
});

const currOption = ref();

const currField = computed(() => {
  const { column } = props.renderParams || {};
  return column ? column.field : '';
});

const load = () => {
  const { renderParams } = props;
  if (renderParams) {
    const { column } = renderParams;
    const option = column.filters[0];
    currOption.value = option;
  }
};

const changeOptionEvent = () => {
  const { renderParams } = props;
  const option = currOption.value;
  if (renderParams && option) {
    const { $table } = renderParams;
    // const checked = !!option.data.text;
    if (option.data.type === 'NAN') {
      currOption.value.data.text = '';
    }
    $table.updateFilterOptionStatus(option, true);
  }
};

const confirmEvent = () => {
  const { renderParams } = props;
  if (renderParams) {
    const { $grid } = renderParams;
    $grid.commitProxy('query');
  }
};

const resetEvent = () => {
  const { renderParams } = props;
  if (renderParams) {
    const { $table, $grid } = renderParams;
    $table.resetFilterPanel();
    $grid.commitProxy('query');
  }
};

watch(currField, () => {
  load();
});

load();
</script>
<template>
  <div v-if="currOption" class="filter-panel">
    <div class="filter-type" v-if="currOption.data.type !== undefined">
      <RadioGroup
        v-model:value="currOption.data.type"
        @change="changeOptionEvent"
        @keyup.enter="confirmEvent"
      >
        <Radio value="NAN">{{ $t('common.null') }}</Radio>
        <Radio value="has">{{ $t('common.has') }}</Radio>
      </RadioGroup>
    </div>
    <div class="filter-input-wrapper">
      <Input
        v-model:value="currOption.data.text"
        :disabled="currOption.data.type === 'NAN'"
        class="filter-input"
        :placeholder="$t('ui.placeholder.input')"
        @input="changeOptionEvent"
        @keyup.enter="confirmEvent"
      />
    </div>
    <div class="filter-actions">
      <Button
        size="small"
        type="primary"
        :disabled="currOption.data.type !== 'NAN' && !currOption.data.text"
        @click="confirmEvent"
      >
        {{ $t('common.filter') }}
      </Button>
      <Button size="small" @click="resetEvent">{{ $t('common.reset') }}</Button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.filter-panel {
  padding: 0;
  background: #fff;
  border-radius: 4px;

  .filter-type {
    margin-bottom: 8px;

    :deep(.ant-radio-group) {
      display: flex;
      gap: 16px;
    }

    :deep(.ant-radio-wrapper) {
      margin: 0;
      font-size: 12px;
      color: #595959;

      .ant-radio {
        .ant-radio-inner {
          width: 14px;
          height: 14px;
        }

        &.ant-radio-checked .ant-radio-inner::after {
          top: 3px;
          left: 3px;
          width: 6px;
          height: 6px;
        }
      }

      span:last-child {
        padding-left: 6px;
      }
    }
  }

  .filter-input-wrapper {
    margin-bottom: 8px;

    .filter-input {
      border-radius: 2px;

      &::placeholder {
        font-size: 14px;
      }
    }
  }

  .filter-actions {
    display: flex;
    gap: 6px;
    justify-content: flex-end;

    :deep(.ant-btn) {
      height: 24px;
      padding: 0 10px;
      font-size: 12px;
      line-height: 22px;
      border-radius: 2px;

      &.ant-btn-sm {
        height: 24px;
        padding: 0 10px;
        font-size: 12px;
        line-height: 22px;
      }
    }
  }
}
</style>
