<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import { IconifyIcon } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { useRequest } from '#/hooks';
import { $t } from '#/locales';

defineOptions({
  name: 'VxeTableExportButton',
});

const props = defineProps<{
  api: (params: Recordable<any>) => Promise<any>;
  gridApi: any;
}>();

const emits = defineEmits(['click']);

const importService = useRequest(props.api, {
  onSuccess: () => {
    const { gridApi } = props;
    gridApi.query();
    message.success($t('common.importSuccess'));
  },
});

async function onClick() {
  // const params = await resolveParams();
  // await downloadService.run(params);
  const { gridApi } = props;
  gridApi.grid.importData({
    types: ['xlsx'],
    modes: ['covering'],
    remote: true,
    async importMethod(options: any) {
      await importService.run(options);
    },
  });
  emits('click');
}
</script>
<template>
  <Button
    type="default"
    shape="circle"
    class="export-btn"
    :loading="importService.loading.value"
    :title="$t('common.import')"
    @click="onClick()"
  >
    <IconifyIcon v-if="!importService.loading.value" icon="carbon:upload" />
  </Button>
</template>

<style lang="less" scoped>
.export-btn {
  height: 30px;
  width: 30px;

  svg {
    position: relative;
    top: -2px;
  }
}
</style>
