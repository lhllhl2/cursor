<script setup lang="ts">
import { computed } from 'vue';

import { useUserStore } from '@vben/stores';

import { $t } from '#/locales';

const userStore = useUserStore();

const greeting = computed(() => {
  const hour = new Date().getHours();
  const periods = [
    [6, 12, 'common.goodMorning'],
    [12, 18, 'common.goodAfternoon'],
    [18, 22, 'common.goodEvening'],
  ] as const;
  const key = periods.find(([s, e]) => hour >= s && hour < e)?.[2] ?? 'common.night';
  return $t(key);
});

const userDisplayName = computed(
  () => userStore.userInfo?.displayName || userStore.userInfo?.username || '用户',
);
</script>

<template>
  <div class="intro-section">
    <div class="intro-main">
      <div class="greeting-section">
        <h1 class="greeting-text">
          {{ greeting }}，<span class="text-primary">{{ userDisplayName }}</span>
        </h1>
        <p class="welcome-text">{{ $t('home.welcome') }}</p>
      </div>
      <div class="system-desc">
        <p class="desc-text">{{ $t('home.description') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.intro-section {
  position: relative;
  display: flex;
  gap: 40px;
  align-items: flex-start;
  padding: 40px;
}

.intro-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.greeting-section {
  .greeting-text {
    margin: 0;
    font-size: 28px;
    font-weight: 600;
    line-height: 1.4;
    color: #262626;
  }

  .welcome-text {
    margin: 8px 0 0;
    font-size: 16px;
    color: #8c8c8c;
  }
}

.system-desc .desc-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #595959;
}

@media (max-width: 1024px) {
  .intro-section {
    flex-direction: column;
    gap: 24px;
    padding: 32px;
  }

  .greeting-section .greeting-text {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .intro-section {
    padding: 24px 20px;
  }

  .greeting-section .greeting-text {
    font-size: 24px;
  }

  .system-desc .desc-text {
    font-size: 13px;
  }
}
</style>
