import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { BudgetType } from '#/api';
import { $t } from '#/locales';

/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

/** 表格类型 */
export type TableType = BudgetType;

/** 表格切换选项 */
export function useTableOptions() {
  return [
    {
      label: $t('report.balance.projectBudget.investment'),
      value: BudgetType.TOTALINVESTM,
    },
    {
      label: $t('report.balance.projectBudget.payment'),
      value: BudgetType.PAYMENT,
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
          title: $t('report.balance.projectBudget.amountYearTotal'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountAdj`,
          title: $t('report.balance.projectBudget.amountAdj'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountTotal`,
          title: $t('report.balance.projectBudget.amountTotal'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountFrozen`,
          title: $t('report.balance.projectBudget.amountFrozen'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountOccupied`,
          title: $t('report.balance.projectBudget.amountOccupied'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountActual`,
          title: $t('report.balance.projectBudget.amountActual'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountAvailable`,
          title: $t('report.balance.projectBudget.amountAvailable'),
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

/** 投资额表格列配置 */
export function useInvestmentColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'ehrCode',
      title: $t('report.balance.projectBudget.ehrCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'ehrName',
      title: $t('report.balance.projectBudget.ehrName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'projectCode',
      title: $t('report.balance.projectBudget.projectCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'projectName',
      title: $t('report.balance.projectBudget.projectName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
      fixed: 'left',
    },
    {
      field: 'total.amountYearTotal',
      title: $t('report.balance.projectBudget.amountYearTotal'),
      width: 150,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'total.amountAdj',
      title: $t('report.balance.projectBudget.amountAdj'),
      width: 150,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'total.amountTotal',
      title: $t('report.balance.projectBudget.amountTotal'),
      width: 150,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'lastYearUsedBudget',
      title: $t('report.balance.projectBudget.lastYearUsedBudget'),
      width: 150,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      title: $t('report.balance.projectBudget.currentYearUsage'),
      children: [
        {
          field: 'total.amountFrozen',
          title: $t('report.balance.projectBudget.amountFrozen'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: 'total.amountOccupied',
          title: $t('report.balance.projectBudget.amountOccupied'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: 'total.amountActual',
          title: $t('report.balance.projectBudget.amountActual'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
      ],
    },
    {
      field: 'total.amountAvailable',
      title: $t('report.balance.projectBudget.amountAvailable'),
      width: 150,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'total.amountActualApproved',
      title: $t('report.balance.amountActualApproved'),
      width: 150,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
  ];
}

/** 付款额表格列配置 */
export function usePaymentColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'ehrCode',
      title: $t('report.balance.projectBudget.ehrCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'ehrName',
      title: $t('report.balance.projectBudget.ehrName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'projectCode',
      title: $t('report.balance.projectBudget.projectCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'projectName',
      title: $t('report.balance.projectBudget.projectName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
      fixed: 'left',
    },
    ...createQuarterColumns('q1', $t('report.balance.projectBudget.q1')),
    ...createQuarterColumns('q2', $t('report.balance.projectBudget.q2')),
    ...createQuarterColumns('q3', $t('report.balance.projectBudget.q3')),
    ...createQuarterColumns('q4', $t('report.balance.projectBudget.q4')),
  ];
}

/** 根据表格类型获取列配置 */
export function useColumnsByType(type: TableType) {
  switch (type) {
    case BudgetType.PAYMENT: {
      return usePaymentColumns();
    }
    case BudgetType.TOTALINVESTM: {
      return useInvestmentColumns();
    }
    default: {
      return useInvestmentColumns();
    }
  }
}
