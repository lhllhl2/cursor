import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { DeptAssetBudgetType } from '#/api';
import { $t } from '#/locales';

import { DetailsTableType } from '../details/data';

/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

/** 表格类型 */
export type TableType = DeptAssetBudgetType;

/** 表格切换选项 */
export function useTableOptions() {
  return [
    {
      label: $t('report.balance.deptAsset.purchase'),
      value: DeptAssetBudgetType.PURCHASE,
    },
    {
      label: $t('report.balance.deptAsset.payment'),
      value: DeptAssetBudgetType.PAYMENT,
    },
  ];
}

/** 创建季度列配置 */
function createQuarterColumns(
  quarter: 'q1' | 'q2' | 'q3' | 'q4',
  quarterLabel: string,
): NonNullable<VxeTableGridOptions<any>['columns']> {
  return [
    {
      title: quarterLabel,
      children: [
        {
          field: `${quarter}.amountYearTotal`,
          title: $t('report.balance.deptAsset.amountYearTotal'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountAdj`,
          title: $t('report.balance.deptAsset.amountAdj'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountTotal`,
          title: $t('report.balance.deptAsset.amountTotal'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountFrozen`,
          title: $t('report.balance.deptAsset.amountFrozen'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountOccupied`,
          title: $t('report.balance.deptAsset.amountOccupied'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountActual`,
          title: $t('report.balance.deptAsset.amountActual'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountAvailable`,
          title: $t('report.balance.deptAsset.amountAvailable'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountActualApproved`,
          title: $t('report.balance.amountActualApproved'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
      ],
    },
  ];
}

/** 采购额表格列配置 */
export function usePurchaseColumns(
  budgetType: string,
): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'ehrCode',
      title: $t('report.balance.deptAsset.ehrCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'ehrName',
      title: $t('report.balance.deptAsset.ehrName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
      fixed: 'left',
      cellRender: {
        name: 'CellLink',
        attrs: {
          name: 'balanceDetails',
          type: 'router',
          query: ['ehrCode', 'ehrName'],
          value: {
            type: DetailsTableType.DeptAsset,
            budgetType,
          },
        },
      },
    },
    {
      field: 'erpAssetType',
      title: $t('report.balance.deptAsset.assetTypeCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'erpAssetTypeName',
      title: $t('report.balance.deptAsset.assetTypeName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    ...createQuarterColumns('q1', $t('report.balance.deptAsset.q1')),
    ...createQuarterColumns('q2', $t('report.balance.deptAsset.q2')),
    ...createQuarterColumns('q3', $t('report.balance.deptAsset.q3')),
    ...createQuarterColumns('q4', $t('report.balance.deptAsset.q4')),
  ];
}

/** 付款额表格列配置 */
export function usePaymentColumns(
  budgetType: string,
): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'ehrCode',
      title: $t('report.balance.deptAsset.ehrCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'ehrName',
      title: $t('report.balance.deptAsset.ehrName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
      fixed: 'left',
      cellRender: {
        name: 'CellLink',
        attrs: {
          name: 'balanceDetails',
          type: 'router',
          query: ['ehrCode', 'ehrName'],
          value: {
            type: DetailsTableType.DeptAsset,
            budgetType,
          },
        },
      },
    },
    {
      field: 'erpAssetType',
      title: $t('report.balance.deptAsset.assetTypeCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'erpAssetTypeName',
      title: $t('report.balance.deptAsset.assetTypeName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    ...createQuarterColumns('q1', $t('report.balance.deptAsset.q1')),
    ...createQuarterColumns('q2', $t('report.balance.deptAsset.q2')),
    ...createQuarterColumns('q3', $t('report.balance.deptAsset.q3')),
    ...createQuarterColumns('q4', $t('report.balance.deptAsset.q4')),
  ];
}

/** 根据表格类型获取列配置 */
export function useColumnsByType(type: TableType) {
  switch (type) {
    case DeptAssetBudgetType.PAYMENT: {
      return usePaymentColumns(type);
    }
    case DeptAssetBudgetType.PURCHASE: {
      return usePurchaseColumns(type);
    }
    default: {
      return usePurchaseColumns();
    }
  }
}
