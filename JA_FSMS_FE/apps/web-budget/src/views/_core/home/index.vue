<script setup lang="ts">
import { Page } from '@vben/common-ui';

import { HomeFinanceBg, HomeGreeting, HomeQuickNav } from './components';
</script>

<template>
  <Page auto-content-height>
    <div class="home-container bg-card rounded-md">
      <HomeFinanceBg />
      <div class="unified-content">
        <HomeGreeting />
        <div class="divider"></div>
        <HomeQuickNav />
      </div>
    </div>
  </Page>
</template>

<style scoped lang="scss">
.home-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.unified-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.divider {
  height: 1px;
  margin: 0 40px;
  background: #f0f0f0;
}

@media (max-width: 768px) {
  .divider {
    margin: 0 20px;
  }
}
</style>
