import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';
/** 业务类型枚举 */
export enum BizType {
  /** 预算调整单 */
  ADJUST = 'ADJUST',
  /** 需求单 */
  APPLY = 'APPLY',
  /** 付款/报销单 */
  CLAIM = 'CLAIM',
  /** 合同单 */
  CONTRACT = 'CONTRACT',
}

export namespace ReceiptApi {
  /** 查询参数 */
  export interface QueryParams {
    pageNo: number;
    pageSize: number;
    bizType: BizType;
    demandOrderNo?: string;
    contractNo?: string;
    claimOrderNo?: string;
    status?: string;
    year?: string;
    quarter?: string;
    month?: string;
    morgCode?: string;
    /** 控制层级EHR组织代码（模糊） */
    controlEhrCd?: string;
    /** 控制层级EHR组织名称（模糊） */
    controlEhrNm?: string;
    /** 预算组织编码（模糊） */
    budgetOrgCd?: string;
    /** 预算组织名称（模糊） */
    budgetOrgNm?: string;
    budgetSubjectCode?: string;
    erpAssetType?: string;
    masterProjectCode?: string;
    isInternal?: string;
  }

  /** 返回数据项 */
  export interface BudgetLedgerItem {
    bizType: string;
    bizTypeDes: string;
    status: string;
    statusDes: string;
    bizCode: string;
    bizItemCode: string;
    effectType: string;
    year: string;
    month: string;
    actualYear: string;
    actualMonth: string;
    morgCode: string;
    /** 控制层级EHR组织代码 */
    controlEhrCd?: string;
    /** 控制层级EHR组织名称 */
    controlEhrNm?: string;
    /** 预算组织编码 */
    budgetOrgCd?: string;
    /** 预算组织名称 */
    budgetOrgNm?: string;
    budgetSubjectCode: string;
    masterProjectCode: string;
    erpAssetType: string;
    isInternal: string;
    isInternalDes: string;
    currency: string;
    amount: number;
    amountAvailable: number;
    version: string;
    versionPre: string;
    createTime: string;
    updateTime: string;
    creator: string;
    updater: string;
    relatedDetails?: BudgetLedgerItem[];
  }

  /** 分页响应 */
  export interface PageResponse {
    list: BudgetLedgerItem[];
    total: number;
  }
}

enum API {
  QUERY_DATA = '/budget-api/query/queryData',
  RECEIPT_EXPORT = '/budget-api/query/export',
}

/**
 * 查询单据明细数据
 */
async function queryReceiptData(params: ReceiptApi.QueryParams) {
  return requestClient.post<ReceiptApi.PageResponse>(API.QUERY_DATA, params);
}

async function exportReceipt(params: Recordable<any>) {
  return requestClient.post(
    API.RECEIPT_EXPORT,
    params,
    {
      responseType: 'blob',
      responseReturn: 'raw',
      // 导出文件可能耗时较长，单独提升超时时间为 10 分钟
      timeout: 600_000,
    },
  );
}

export { exportReceipt, queryReceiptData };
