import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { BizType } from '#/api';
import { $t } from '#/locales';
import { monthOptions, yearOptions } from '#/utils';

/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

const yearFilters = [...yearOptions(5)];
// const quarterFilters = [...quarterOptions(4)];
const monthFilters = [...monthOptions(12)];

/** 表格类型（直接使用后端 BizType） */
export type TableType = BizType;

/** 表格切换选项 */
export function useTableOptions() {
  return [
    { label: $t('report.receipt.demand'), value: BizType.APPLY },
    { label: $t('report.receipt.contract'), value: BizType.CONTRACT },
    { label: $t('report.receipt.payment'), value: BizType.CLAIM },
    { label: $t('report.receipt.adjust'), value: BizType.ADJUST },
  ];
}

/** 需求单表格列配置 */
export function useDemandColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'bizCode',
      title: $t('report.receipt.demandNo'),
      width: 120,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'status',
      title: $t('report.receipt.status'),
      width: 120,
      filters: [],
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'year',
      title: $t('report.receipt.demandYear'),
      filters: yearFilters,
      width: 100,
    },
    // {
    //   field: 'quarter',
    //   title: $t('report.receipt.demandQuarter'),
    //   filters: quarterFilters,
    //   width: 100,
    // },
    {
      field: 'month',
      title: $t('report.receipt.demandMonth'),
      filters: monthFilters,
      width: 100,
    },
    {
      field: 'morgCode',
      title: $t('report.receipt.ehrCode'),
      filters: [{ data: { text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'morgName',
      title: $t('report.receipt.ehrName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 150,
    },
    {
      field: 'controlEhrCd',
      title: $t('report.receipt.controlEhrOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 160,
    },
    {
      field: 'controlEhrNm',
      title: $t('report.receipt.controlEhrOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetOrgCd',
      title: $t('report.receipt.budgetOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 140,
    },
    {
      field: 'budgetOrgNm',
      title: $t('report.receipt.budgetOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetSubjectCode',
      title: $t('report.receipt.expenseCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'budgetSubjectName',
      title: $t('report.receipt.expenseName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'erpAssetType',
      title: $t('report.receipt.assetTypeCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 150,
    },
    {
      field: 'erpAssetTypeName',
      title: $t('report.receipt.assetTypeName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 150,
    },
    {
      field: 'masterProjectCode',
      title: $t('report.receipt.projectCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'masterProjectName',
      title: $t('report.receipt.projectName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'isInternal',
      title: $t('report.receipt.internal'),
      width: 120,
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'operator',
      title: $t('report.receipt.operator'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'updateTime',
      title: $t('report.receipt.updateTime'),
      width: 160,
    },
    {
      field: 'dataSource',
      title: $t('report.receipt.dataSource'),
      width: 120,
    },
    {
      field: 'processName',
      title: $t('report.receipt.processName'),
      width: 120,
    },
    {
      field: 'amount',
      title: $t('report.receipt.demandAmount'),
      width: 120,
      align: 'right',
      fixed: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'amountAvailable',
      title: $t('report.receipt.availableAmount'),
      width: 120,
      align: 'right',
      fixed: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
  ];
}

/** 合同单表格列配置 */
export function useContractColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'bizCode',
      title: $t('report.receipt.contractNo'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'demandOrderNo',
      title: $t('report.receipt.demandNo'),
      minWidth: 100,
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      slots: { default: 'demandOrderNos' },
    },
    {
      field: 'status',
      title: $t('report.receipt.status'),
      width: 120,
      filters: [],
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'year',
      title: $t('report.receipt.contractYear'),
      filters: yearFilters,
      width: 100,
    },
    // {
    //   field: 'quarter',
    //   title: $t('report.receipt.contractQuarter'),
    //   filters: quarterFilters,
    //   width: 100,
    // },
    {
      field: 'month',
      title: $t('report.receipt.contractMonth'),
      filters: monthFilters,
      width: 100,
    },
    {
      field: 'morgCode',
      title: $t('report.receipt.ehrCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'morgName',
      title: $t('report.receipt.ehrName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'controlEhrCd',
      title: $t('report.receipt.controlEhrOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 160,
    },
    {
      field: 'controlEhrNm',
      title: $t('report.receipt.controlEhrOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetOrgCd',
      title: $t('report.receipt.budgetOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 140,
    },
    {
      field: 'budgetOrgNm',
      title: $t('report.receipt.budgetOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetSubjectCode',
      title: $t('report.receipt.expenseCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'budgetSubjectName',
      title: $t('report.receipt.expenseName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'erpAssetType',
      title: $t('report.receipt.assetTypeCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 150,
    },
    {
      field: 'erpAssetTypeName',
      title: $t('report.receipt.assetTypeName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 150,
    },
    {
      field: 'masterProjectCode',
      title: $t('report.receipt.projectCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'masterProjectName',
      title: $t('report.receipt.projectName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'isInternal',
      title: $t('report.receipt.internal'),
      width: 120,
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'operator',
      title: $t('report.receipt.operator'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'updateTime',
      title: $t('report.receipt.updateTime'),
      width: 160,
    },
    {
      field: 'dataSource',
      title: $t('report.receipt.dataSource'),
      width: 120,
    },
    {
      field: 'processName',
      title: $t('report.receipt.processName'),
      width: 120,
    },
    {
      field: 'amount',
      title: $t('report.receipt.contractAmount'),
      width: 120,
      fixed: 'right',
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'amountAvailable',
      title: $t('report.receipt.unpaidAmount'),
      width: 120,
      fixed: 'right',
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
  ];
}

/** 付款/报销单表格列配置 */
export function usePaymentColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'bizCode',
      title: $t('report.receipt.paymentNo'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'contractNo',
      title: $t('report.receipt.contractNo'),
      minWidth: 100,
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      slots: { default: 'contractNos' },
    },
    {
      field: 'demandOrderNo',
      title: $t('report.receipt.demandNo'),
      minWidth: 100,
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      slots: { default: 'demandOrderNos' },
    },
    {
      field: 'status',
      title: $t('report.receipt.status'),
      width: 120,
      filters: [],
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'year',
      title: $t('report.receipt.actualYear'),
      filters: yearFilters,
      width: 120,
    },
    // {
    //   field: 'quarter',
    //   title: $t('report.receipt.actualQuarter'),
    //   filters: quarterFilters,
    //   width: 120,
    // },
    {
      field: 'month',
      title: $t('report.receipt.actualMonth'),
      filters: monthFilters,
      width: 120,
    },
    {
      field: 'morgCode',
      title: $t('report.receipt.ehrCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'morgName',
      title: $t('report.receipt.ehrName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'controlEhrCd',
      title: $t('report.receipt.controlEhrOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 160,
    },
    {
      field: 'controlEhrNm',
      title: $t('report.receipt.controlEhrOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetOrgCd',
      title: $t('report.receipt.budgetOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 140,
    },
    {
      field: 'budgetOrgNm',
      title: $t('report.receipt.budgetOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetSubjectCode',
      title: $t('report.receipt.expenseCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'budgetSubjectName',
      title: $t('report.receipt.expenseName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'erpAssetType',
      title: $t('report.receipt.assetTypeCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 150,
    },
    {
      field: 'erpAssetTypeName',
      title: $t('report.receipt.assetTypeName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 150,
    },
    {
      field: 'masterProjectCode',
      title: $t('report.receipt.projectCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'masterProjectName',
      title: $t('report.receipt.projectName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'isInternal',
      title: $t('report.receipt.internal'),
      width: 120,
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'operator',
      title: $t('report.receipt.operator'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'updateTime',
      title: $t('report.receipt.updateTime'),
      width: 160,
    },
    {
      field: 'dataSource',
      title: $t('report.receipt.dataSource'),
      width: 120,
    },
    {
      field: 'processName',
      title: $t('report.receipt.processName'),
      width: 120,
    },
    {
      field: 'amount',
      title: $t('report.receipt.paymentAmount'),
      width: 130,
      fixed: 'right',
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
  ];
}

/** 预算调整单表格列配置 */
export function useAdjustColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'bizCode',
      title: $t('report.receipt.adjustNo'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'effectType',
      title: $t('report.receipt.adjustType'),
      width: 120,
      filters: [],
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'status',
      title: $t('report.receipt.status'),
      width: 120,
      filters: [],
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'year',
      title: $t('report.receipt.adjustYear'),
      filters: yearFilters,
      width: 120,
    },
    // {
    //   field: 'quarter',
    //   title: $t('report.receipt.adjustQuarter'),
    //   filters: quarterFilters,
    //   width: 120,
    // },
    {
      field: 'month',
      title: $t('report.receipt.adjustMonth'),
      filters: monthFilters,
      width: 120,
    },
    {
      field: 'morgCode',
      title: $t('report.receipt.ehrCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'morgName',
      title: $t('report.receipt.ehrName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'controlEhrCd',
      title: $t('report.receipt.controlEhrOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 160,
    },
    {
      field: 'controlEhrNm',
      title: $t('report.receipt.controlEhrOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetOrgCd',
      title: $t('report.receipt.budgetOrgCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 140,
    },
    {
      field: 'budgetOrgNm',
      title: $t('report.receipt.budgetOrgName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 160,
    },
    {
      field: 'budgetSubjectCode',
      title: $t('report.receipt.expenseCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'budgetSubjectName',
      title: $t('report.receipt.expenseName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'erpAssetType',
      title: $t('report.receipt.assetTypeCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 150,
    },
    {
      field: 'erpAssetTypeName',
      title: $t('report.receipt.assetTypeName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 150,
    },
    {
      field: 'masterProjectCode',
      title: $t('report.receipt.projectCode'),
      filters: [{ data: { type: 'has', text: '' } }],
      filterRender: {
        name: 'InputFilter',
      },
      width: 120,
    },
    {
      field: 'masterProjectName',
      title: $t('report.receipt.projectName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 120,
    },
    {
      field: 'operator',
      title: $t('report.receipt.operator'),
      width: 150,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'updateTime',
      title: $t('report.receipt.updateTime'),
      width: 160,
    },
    {
      field: 'dataSource',
      title: $t('report.receipt.dataSource'),
      width: 120,
    },
    {
      field: 'processName',
      title: $t('report.receipt.processName'),
      width: 120,
    },
    {
      field: 'amountConsumedQOne',
      title: $t('report.receipt.quarterOne'),
      width: 120,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'amountConsumedQTwo',
      title: $t('report.receipt.quarterTwo'),
      width: 120,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'amountConsumedQThree',
      title: $t('report.receipt.quarterThree'),
      width: 120,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'amountConsumedQFour',
      title: $t('report.receipt.quarterFour'),
      width: 120,
      align: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
    {
      field: 'amount',
      title: $t('report.receipt.adjustAmount'),
      width: 120,
      align: 'right',
      fixed: 'right',
      cellRender: {
        name: 'CellMoney',
      },
    },
  ];
}

/** 根据表格类型获取列配置 */
export function useColumnsByType(type: TableType) {
  switch (type) {
    case BizType.ADJUST: {
      return useAdjustColumns();
    }
    case BizType.APPLY: {
      return useDemandColumns();
    }
    case BizType.CLAIM: {
      return usePaymentColumns();
    }
    case BizType.CONTRACT: {
      return useContractColumns();
    }
    default: {
      return useDemandColumns();
    }
  }
}
