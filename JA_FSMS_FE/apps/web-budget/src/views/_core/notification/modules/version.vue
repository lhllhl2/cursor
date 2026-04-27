<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';

import { Card, Tag } from 'ant-design-vue';

import { $t } from '#/locales';

import { versionData } from '../data';

const [Modal, modalApi] = useVbenModal({
  onCancel() {
    modalApi.close();
  },
  async onOpenChange() {},
  footer: false,
});
</script>
<template>
  <Modal :title="$t('common.versionLog')" class="w-[700px]" :z-index="9999">
    <div v-for="(item, index) in versionData" :key="item.title">
      <Card :title="item.title" class="mb-4">
        <template #extra v-if="!index">
          <Tag color="blue">{{ $t('common.latest') }}</Tag>
        </template>
        <ul class="list-inside list-disc">
          <li v-for="content in item.content" :key="content">
            <span>{{ content }}</span>
          </li>
        </ul>
      </Card>
    </div>
  </Modal>
</template>
