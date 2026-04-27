<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import { IconifyIcon } from '@vben/icons';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message } from 'ant-design-vue';

import { useRequest } from '#/hooks';
import { $t } from '#/locales';

type ParamsInput =
  | (() => Promise<Record<string, any>> | Record<string, any>)
  | Record<string, any>;

defineOptions({
  name: 'VxeTableExportButton',
});

const props = defineProps<{
  api: (params: Recordable<any>) => Promise<any>;
  params?: ParamsInput;
}>();

const emits = defineEmits(['click']);

const downloadService = useRequest(props.api, {
  onSuccess: (res: any) => {
    const { headers, data } = res;
    let fileName = 'download.xlsx'; // 默认文件名，Excel文件

    // 尝试从响应头获取文件名
    const contentDisposition = headers['content-disposition'];
    const standardMatch = contentDisposition.match(/filename=([^"]+)/);
    if (standardMatch) {
      fileName = decodeURIComponent(standardMatch[1]);
    }
    downloadFileFromBlobPart({
      source: data,
      fileName,
    });
    message.success($t('common.exportSuccess', [fileName]));
    emits('click');
  },
});

async function resolveParams(): Promise<Record<string, any>> {
  if (!props.params) return {};
  if (typeof props.params === 'function') {
    const r = await props.params();
    return r ?? {};
  }
  return props.params;
}

async function onClick() {
  const params = await resolveParams();
  await downloadService.run(params);
  emits('click');
}
</script>
<template>
  <Button
    type="default"
    shape="circle"
    class="export-btn"
    :loading="downloadService.loading.value"
    :title="$t('common.export')"
    @click="onClick()"
  >
    <IconifyIcon v-if="!downloadService.loading.value" icon="carbon:download" />
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
