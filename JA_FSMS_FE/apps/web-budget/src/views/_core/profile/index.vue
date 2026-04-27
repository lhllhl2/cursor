<script setup lang="ts">
import type { Component } from 'vue';

import { computed, ref } from 'vue';

import { useUserStore } from '@vben/stores';

import ProfileBase from './base-setting.vue';
import ProfileNotificationSetting from './notification-setting.vue';
import ProfilePasswordSetting from './password-setting.vue';
import ProfileSecuritySetting from './security-setting.vue';

const userStore = useUserStore();

const tabComponents: Record<string, Component> = {
  basic: ProfileBase,
  security: ProfileSecuritySetting,
  password: ProfilePasswordSetting,
  notice: ProfileNotificationSetting,
};

const tabs = [
  { label: '基本设置', value: 'basic' },
  { label: '安全设置', value: 'security' },
  { label: '修改密码', value: 'password' },
  { label: '新消息提醒', value: 'notice' },
];

type TabValue = (typeof tabs)[number]['value'];

const tabsValue = ref<TabValue>('basic');
const userInfo = computed(() => userStore.userInfo);
</script>
<template>
  <div class="space-y-6">
    <div
      class="flex flex-col gap-4 rounded-lg border border-solid border-gray-200 bg-white p-6 shadow-sm md:flex-row md:items-center dark:border-gray-700 dark:bg-gray-900"
    >
      <div
        class="bg-primary/10 text-primary flex size-16 items-center justify-center rounded-full text-xl font-semibold"
      >
        {{
          userInfo?.realName?.charAt(0) ?? userInfo?.username?.charAt(0) ?? 'U'
        }}
      </div>
      <div class="space-y-1">
        <div class="text-xl font-semibold">
          {{ userInfo?.realName ?? userInfo?.username ?? '用户' }}
        </div>
        <div class="text-sm text-gray-500">
          {{ userInfo?.desc ?? '欢迎使用晶澳财务共享管理系统' }}
        </div>
      </div>
    </div>

    <a-tabs v-model:active-key="tabsValue">
      <a-tab-pane v-for="tab in tabs" :key="tab.value" :tab="tab.label">
        <component :is="tabComponents[tab.value]" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
