<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import { computed } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { Notification } from '@vben/layouts';

import { notifications } from './data';
import Version from './modules/version.vue';

const [VersionModal, versionModalApi] = useVbenModal({
  connectedComponent: Version,
  destroyOnClose: true,
});

const showDot = computed(() =>
  notifications.value.some((item) => !item.isRead),
);
function handleNoticeClear() {
  notifications.value = [];
}

function handleMakeAll() {
  notifications.value.forEach((item) => (item.isRead = true));
}

function handleRead(item: NotificationItem) {
  item.isRead = true;
  versionModalApi.open();
}
</script>

<template>
  <VersionModal />
  <Notification
    :dot="showDot"
    :notifications="notifications"
    @clear="handleNoticeClear"
    @make-all="handleMakeAll"
    @read="handleRead"
  />
</template>
