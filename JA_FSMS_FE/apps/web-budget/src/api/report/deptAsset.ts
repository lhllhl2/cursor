import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

/** 部门资产预算类型枚举 */
export enum DeptAssetBudgetType {
  /** 付款额 */
  PAYMENT = 'PAYMENT',
  /** 采购额 */
  PURCHASE = 'PURCHASE',
}

export namespace DeptAssetBalanceApi {
  /** 预算金额对象 */
  export interface BudgetAmount {
    amountTotal: number;
    amountYearTotal: number;
    amountAdj: number;
    amountFrozen: number;
    amountOccupied: number;
    amountActual: number;
    amountAvailable: number;
  }

  /** 查询参数 */
  export interface QueryParams {
    pageNo: number;
    pageSize: number;
    budgetType: DeptAssetBudgetType;
    year?: string;
    ehrCode?: string;
    ehrName?: string;
    erpAssetType?: string;
  }

  /** 返回数据项 */
  export interface BudgetBalanceItem {
    ehrCode: string;
    ehrName: string;
    subjectCode: string;
    subjectName: string;
    erpAssetType: string;
    erpAssetTypeName: string;
    projectCode: string;
    projectName: string;
    isInternal: string;
    q1: BudgetAmount;
    q2: BudgetAmount;
    q3: BudgetAmount;
    q4: BudgetAmount;
    total: BudgetAmount;
  }

  /** 分页响应 */
  export interface PageResponse {
    list: BudgetBalanceItem[];
    total: number;
  }
}

enum API {
  ASSET_QUARTERLY_DETAIL = '/budget-api/query/queryBudgetQuarterlyAggregateByMorg',
  EXPORT_ASSET_QUARTERLY_DETAIL = '/budget-api/query/exportBudgetQuarterlyAggregateByMorg', // 部门资产明细导出
  EXPORT_DEPT_ASSET_BUDGET_BALANCE = '/budget-api/query/exportDeptAssetBudgetBalance',
  QUERY_DEPT_ASSET_BUDGET_BALANCE = '/budget-api/query/queryDeptAssetBudgetBalance',
}

/**
 * 查询部门资产可用预算数据
 */
async function queryDeptAssetBudgetBalance(
  params: DeptAssetBalanceApi.QueryParams,
) {
  return requestClient.post<DeptAssetBalanceApi.PageResponse>(
    API.QUERY_DEPT_ASSET_BUDGET_BALANCE,
    params,
  );
}

/**
 * 导出部门资产可用预算数据
 */
async function exportDeptAssetBudgetBalance(params: Recordable<any>) {
  return requestClient.post(API.EXPORT_DEPT_ASSET_BUDGET_BALANCE, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

/**
 * 部门资产明细页
 */
async function getAssetQuarterlyDetail(params: any) {
  return requestClient.post<DeptAssetBalanceApi.PageResponse>(
    API.ASSET_QUARTERLY_DETAIL,
    params,
  );
}

/**
 * 导出部门资产明细数据
 */
async function exportAssetQuarterlyDetail(params: Recordable<any>) {
  return requestClient.post(API.EXPORT_ASSET_QUARTERLY_DETAIL, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

export {
  exportAssetQuarterlyDetail,
  exportDeptAssetBudgetBalance,
  getAssetQuarterlyDetail,
  queryDeptAssetBudgetBalance,
};
