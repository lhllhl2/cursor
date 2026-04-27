import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';

/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

/** 创建月份列配置 */
function createMonthColumns(): VxeTableGridOptions<any>['columns'] {
  const months = [
    { field: 'paymentAmount.amount01', label: $t('report.payment.month1') },
    { field: 'paymentAmount.amount02', label: $t('report.payment.month2') },
    { field: 'paymentAmount.amount03', label: $t('report.payment.month3') },
    { field: 'paymentAmount.amount04', label: $t('report.payment.month4') },
    { field: 'paymentAmount.amount05', label: $t('report.payment.month5') },
    { field: 'paymentAmount.amount06', label: $t('report.payment.month6') },
    { field: 'paymentAmount.amount07', label: $t('report.payment.month7') },
    { field: 'paymentAmount.amount08', label: $t('report.payment.month8') },
    { field: 'paymentAmount.amount09', label: $t('report.payment.month9') },
    { field: 'paymentAmount.amount10', label: $t('report.payment.month10') },
    { field: 'paymentAmount.amount11', label: $t('report.payment.month11') },
    { field: 'paymentAmount.amount12', label: $t('report.payment.month12') },
    {
      field: 'paymentAmount.totalAmount',
      label: $t('report.payment.totalAmount'),
    },
  ];

  return [
    {
      title: $t('report.payment.paymentAmount'),
      children: months.map((month) => ({
        field: month.field,
        title: month.label,
        width: 120,
        align: 'right',
        cellRender: {
          name: 'CellMoney',
        },
      })),
    },
  ];
}

/** 表格列配置 */
export function useColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'projectCode',
      title: $t('report.payment.projectCode'),
      width: 150,
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      align: 'left',
      fixed: 'left',
    },
    {
      field: 'projectName',
      title: $t('report.payment.projectName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
      fixed: 'left',
    },
    {
      field: 'ehrCode',
      title: $t('report.payment.ehrCode'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
    },
    {
      field: 'ehrName',
      title: $t('report.payment.ehrName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
    },
    {
      field: 'erpAcctCd',
      title: $t('report.payment.expenseSubjectCode'),
      width: 150,
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      align: 'left',
    },
    {
      field: 'erpAcctNm',
      title: $t('report.payment.expenseSubjectName'),
      width: 200,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      align: 'left',
    },
    {
      field: 'erpAssetType',
      title: $t('report.payment.assetType'),
      width: 150,
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      align: 'left',
    },
    {
      field: 'isInternal',
      title: $t('report.receipt.internal'),
      width: 120,
      cellRender: {
        name: 'DictCell',
      },
    },
    ...createMonthColumns(),
  ];
}
