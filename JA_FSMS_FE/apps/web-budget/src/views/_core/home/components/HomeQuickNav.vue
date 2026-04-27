<script setup lang="ts">
import { useRouter } from 'vue-router';

import { IconifyIcon } from '@vben/icons';

import { $t } from '#/locales';

export interface QuickNavItem {
  title: string;
  icon: string;
  color: string;
  bgColor: string;
  path: string;
  type?: 'external';
  description: string;
}

const router = useRouter();

const quickNavItems: QuickNavItem[] = [
  {
    title: $t('home.quickNavTitle1'),
    icon: 'mdi:file-document-edit',
    color: '#1890ff',
    bgColor: '#f0f7ff',
    type: 'external',
    path: 'https://budgetplanning.jasolar.com/workspace/index.jsp',
    description: $t('home.quickNavDescription1'),
  },
  {
    title: $t('home.quickNavTitle2'),
    icon: 'mdi:file-chart',
    color: '#52c41a',
    bgColor: '#f6ffed',
    path: '/report/balance/deptBudget',
    description: $t('home.quickNavDescription2'),
  },
];

function handleClick(item: QuickNavItem) {
  item.type === 'external' ? window.open(item.path, '_blank') : router.push(item.path);
}
</script>

<template>
  <div class="quick-nav-section">
    <div class="section-title">
      <IconifyIcon icon="mdi:rocket-launch" class="text-primary" />
      <span>快捷操作</span>
    </div>
    <div class="quick-nav-grid">
      <div
        v-for="item in quickNavItems"
        :key="item.title"
        class="quick-nav-item"
        :style="{ background: item.bgColor }"
        @click="handleClick(item)"
      >
        <div class="quick-nav-content">
          <div class="quick-nav-icon-wrapper">
            <IconifyIcon :icon="item.icon" :style="{ color: item.color }" class="quick-nav-icon" />
          </div>
          <div class="quick-nav-text">
            <h3 class="quick-nav-title" :style="{ color: item.color }">
              {{ item.title }}
            </h3>
            <p class="quick-nav-desc" :style="{ color: item.color }">
              {{ item.description }}
            </p>
          </div>
          <div class="quick-nav-arrow" :style="{ color: item.color }">
            <IconifyIcon icon="mdi:arrow-right" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.quick-nav-section {
  padding: 32px 40px 40px;
}

.section-title {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.quick-nav-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.quick-nav-item {
  position: relative;
  padding: 24px;
  cursor: pointer;
  border: 1px solid transparent;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgb(0 0 0 / 8%);
    transform: translateY(-2px);

    .quick-nav-arrow {
      transform: translateX(4px);
    }
  }
}

.quick-nav-content {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 20px;
  align-items: center;
}

.quick-nav-icon-wrapper {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 160px;
  background: rgb(255 255 255 / 80%);
  border-radius: 8px;
}

.quick-nav-icon {
  font-size: 32px;
}

.quick-nav-text {
  flex: 1;
  min-width: 0;
}

.quick-nav-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.4;
}

.quick-nav-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  opacity: 0.8;
}

.quick-nav-arrow {
  flex-shrink: 0;
  font-size: 20px;
  opacity: 0.6;
  transition: transform 0.2s ease;
}

@media (max-width: 1024px) {
  .quick-nav-section {
    padding: 24px 32px 32px;
  }

  .quick-nav-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .quick-nav-section {
    padding: 20px;
  }

  .section-title {
    margin-bottom: 20px;
    font-size: 16px;
  }

  .quick-nav-item {
    padding: 20px;
  }

  .quick-nav-icon-wrapper {
    width: 56px;
    height: 56px;
  }

  .quick-nav-icon {
    font-size: 28px;
  }

  .quick-nav-title {
    font-size: 18px;
  }

  .quick-nav-desc {
    font-size: 13px;
  }
}
</style>
