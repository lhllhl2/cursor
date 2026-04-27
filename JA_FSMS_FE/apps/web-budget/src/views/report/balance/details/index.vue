<script lang="ts" setup>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { VxeTableExportButton } from '#/adapter/component';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportAssetQuarterlyDetail,
  exportBudgetQuarterlyDetail,
  getAssetQuarterlyDetail,
  getBudgetQuarterlyDetail,
} from '#/api';
import { useRequest } from '#/hooks';
import { $t } from '#/locales';

import {
  DetailsTableType,
  DetailsTableTypeText,
  useColumnsByType,
} from './data';

const { setTabTitle } = useTabs();
const route = useRoute();
const { ehrCode, type, ehrName, budgetType } = route.query;

const service = useRequest(
  type === DetailsTableType.DeptBudget
    ? getBudgetQuarterlyDetail
    : getAssetQuarterlyDetail,
);

/** 导出接口 */
const exportApi = computed(() => {
  return type === DetailsTableType.DeptBudget
    ? exportBudgetQuarterlyDetail
    : exportAssetQuarterlyDetail;
});

/** 导出参数 */
const exportParams = () => {
  const params = {
    controlEhrCd: ehrCode,
    year: new Date().getFullYear().toString(),
  };
  if (budgetType) {
    params.budgetType = budgetType;
  }
  return params;
};

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumnsByType(type),
    keepSource: false,
    height: 'auto',
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          const params = {
            controlEhrCd: ehrCode,
            year: new Date().getFullYear().toString(),
          };
          if (budgetType) {
            params.budgetType = budgetType;
          }
          const response = await service.run(params);
          return response;
        },
      },
    },
    toolbarConfig: {
      zoom: true,
    },
  },
});

onMounted(() => {
  setTabTitle(`${ehrName}-${$t('report.balance.balanceDetails.title')}`);
});
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #table-title>
        <div class="details-header-simple">
          <!-- EHR 信息组 -->
          <div class="ehr-info-group">
            <div class="ehr-code-badge">
              {{ ehrCode }}
            </div>
            <div class="ehr-name">
              {{ ehrName }}
            </div>
          </div>

          <div class="divider-vertical"></div>
          <div class="year-tag">
            {{ new Date().getFullYear() }}
          </div>
          <div class="type-tag">
            {{ DetailsTableTypeText[type] }}
          </div>

          <div
            v-if="budgetType"
            class="category-tag"
            :class="budgetType === 'PURCHASE' ? 'purchase-tag' : 'payment-tag'"
          >
            {{
              budgetType === 'PURCHASE'
                ? $t('report.balance.deptAsset.purchase')
                : $t('report.balance.deptAsset.payment')
            }}
          </div>
        </div>
      </template>
      <template #toolbar-tools>
        <VxeTableExportButton :api="exportApi" :params="exportParams" />
      </template>
    </Grid>
  </Page>
</template>

<style lang="less" scoped>
// ==================== 扁平化表头样式 ====================
.details-header-simple {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

// EHR 信息组 - 编码和名称作为一个整体
.ehr-info-group {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  background: #f8fafc;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

// EHR 编码徽章 - 扁平化
.ehr-code-badge {
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
  letter-spacing: 0.5px;
}

// 部门名称
.ehr-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.3;
}

// 竖线分隔符
.divider-vertical {
  width: 1px;
  height: 20px;
  background: #e5e7eb;
  flex-shrink: 0;
}

// 标签样式 - 扁平化
.type-tag,
.category-tag,
.year-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.type-tag {
  background: #3b82f6;
  color: white;
}

.purchase-tag {
  background: #10b981;
  color: white;
}

.payment-tag {
  background: #f59e0b;
  color: white;
}

.year-tag {
  background: #8b5cf6;
  color: white;
  font-family: 'Consolas', 'Monaco', monospace;
}

// 响应式设计
@media (max-width: 768px) {
  .details-header-simple {
    gap: 12px;
  }

  .ehr-info-group {
    gap: 8px;
    padding: 5px 10px;
  }

  .ehr-code-badge {
    font-size: 12px;
    padding: 2px 6px;
  }

  .ehr-name {
    font-size: 14px;
  }

  .divider-vertical {
    display: none;
  }

  .type-tag,
  .category-tag,
  .year-tag {
    font-size: 12px;
    padding: 4px 10px;
  }
}

// ==================== 原有表格样式 ====================
:deep(.row-total) {
  font-weight: 600;
  color: hsl(var(--primary));
}

:deep(.row--level-0) {
  background-color: #e6f4ff;
  color: hsl(var(--primary));
  font-weight: 600;
  font-size: 14px;

  td {
    border-bottom: 2px solid #91caff;
  }
}

.amount-positive {
  color: #52c41a;
}

.amount-negative {
  color: #ff4d4f;
}

.amount-zero {
  color: #323639;
}

.exception-header-card {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.diff-amount-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.diff-amount-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #64748b;
  font-weight: 500;

  .label-icon {
    font-size: 16px;
    color: hsl(var(--primary));
  }
}

.diff-amount-value {
  margin-left: 6px;
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.5px;
  transition: transform 0.2s ease;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  color: #94a3b8;
  min-width: 60px;
}

.info-value {
  font-weight: 600;
  color: rgba(2, 8, 23, 0.88);
}

.info-tag {
  margin: 0;
  font-weight: 500;
  border-radius: 6px;
}
</style>
