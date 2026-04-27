import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace PaymentStatusApi {
  /** 查询参数 */
  export interface QueryParams {
    pageNo: number;
    pageSize: number;
    year: string;
    ehrCode: string;
    ehrName: string;
    projectCode: string;
    projectName: string;
    erpAcctCd: string;
    erpAcctNm: string;
    erpAssetType: string;
  }

  /** 返回数据项 */
  export interface PaymentStatusItem {
    year: string;
    ehrCode: string;
    ehrName: string;
    erpAcctCd: string;
    erpAcctNm: string;
    erpAssetType: string;
    projectCode: string;
    projectName: string;
    paymentAmount: {
      amount01: number;
      amount02: number;
      amount03: number;
      amount04: number;
      amount05: number;
      amount06: number;
      amount07: number;
      amount08: number;
      amount09: number;
      amount10: number;
      amount11: number;
      amount12: number;
    };
  }

  /** 分页响应 */
  export interface PageResponse {
    list: PaymentStatusItem[];
    total: number;
  }
}

enum API {
  EXPORT_PAYMENT_STATUS = '/budget-api/query/exportPaymentStatus',
  QUERY_PAYMENT_STATUS = '/budget-api/query/queryPaymentStatus',
}

/**
 * 查询付款情况数据
 * 目前接口未完善，使用 mock 数据
 */
async function queryPaymentStatus(
  params: any,
): Promise<PaymentStatusApi.PageResponse> {
  return requestClient.post<PaymentStatusApi.PageResponse>(
    API.QUERY_PAYMENT_STATUS,
    params,
  );
}

/**
 * 导出付款情况数据
 */
async function exportPaymentStatus(params: Recordable<any>) {
  return requestClient.post(API.EXPORT_PAYMENT_STATUS, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

export { exportPaymentStatus, queryPaymentStatus };
