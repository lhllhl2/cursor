import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';

import { DetailsTableType } from '../details/data';
/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

/** 创建季度列配置 */
function createQuarterColumns(
  quarter: 'q1' | 'q2' | 'q3' | 'q4',
  quarterLabel: string,
): VxeTableGridOptions<any>['columns'] {
  return [
    {
      title: quarterLabel,
      children: [
        {
          field: `${quarter}.amountYearTotal`,
          title: $t('report.balance.deptBudget.amountYearTotal'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountAdj`,
          title: $t('report.balance.deptBudget.amountAdj'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountTotal`,
          title: $t('report.balance.deptBudget.amountTotal'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountFrozen`,
          title: $t('report.balance.deptBudget.amountFrozen'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountOccupied`,
          title: $t('report.balance.deptBudget.amountOccupied'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountActual`,
          title: $t('report.balance.deptBudget.amountActual'),
          width: 120,
          align: 'right',
          cellRender: {
            name: 'CellMoney',
          },
        },
        {
          field: `${quarter}.amountAvailable`,
          title: $t('report.balance.deptBudget.amountAvailable'),
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

/** 表格列配置 */
export function useColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'ehrCode',
      title: $t('report.balance.deptBudget.ehrCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      fixed: 'left',
    },
    {
      field: 'ehrName',
      title: $t('report.balance.deptBudget.ehrName'),
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
            type: DetailsTableType.DeptBudget,
          },
        },
      },
    },
    {
      field: 'controlCust1Cd',
      title: $t('report.balance.deptBudget.controlCust1Cd'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'controlCust1Name',
      title: $t('report.balance.deptBudget.controlCust1Name'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'subjectCode',
      title: $t('report.balance.deptBudget.subjectCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'subjectName',
      title: $t('report.balance.deptBudget.subjectName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    ...createQuarterColumns('q1', $t('report.balance.deptBudget.q1')),
    ...createQuarterColumns('q2', $t('report.balance.deptBudget.q2')),
    ...createQuarterColumns('q3', $t('report.balance.deptBudget.q3')),
    ...createQuarterColumns('q4', $t('report.balance.deptBudget.q4')),
  ];
}
