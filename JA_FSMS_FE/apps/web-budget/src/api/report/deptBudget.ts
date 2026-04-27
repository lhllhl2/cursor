import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace DeptBudgetBalanceApi {
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
    ehrCode?: string;
    ehrName?: string;
    customCode?: string;
    customName?: string;
    accountSubjectCode?: string;
    accountSubjectName?: string;
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
  BUDGET_QUARTERLY_DETAIL = '/budget-api/query/queryBudgetQuarterlyDetail', // 部门费用明细页
  EXPORT_BUDGET_QUARTERLY_DETAIL = '/budget-api/query/exportBudgetQuarterlyDetail', // 部门费用明细导出
  EXPORT_DEPT_BUDGET_BALANCE = '/budget-api/query/exportDeptBudgetBalance',
  QUERY_DEPT_BUDGET_BALANCE = '/budget-api/query/queryDeptBudgetBalance',
}

/**
 * 查询部门费用可用预算数据
 */
async function queryDeptBudgetBalance(
  params: DeptBudgetBalanceApi.QueryParams,
) {
  return requestClient.post<DeptBudgetBalanceApi.PageResponse>(
    API.QUERY_DEPT_BUDGET_BALANCE,
    params,
  );
}

/**
 * 导出部门费用可用预算数据
 */
async function exportDeptBudgetBalance(params: Recordable<any>) {
  return requestClient.post(API.EXPORT_DEPT_BUDGET_BALANCE, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

/**
 * 部门费用明细页
 */
async function getBudgetQuarterlyDetail(params: any) {
  return requestClient.post<DeptBudgetBalanceApi.PageResponse>(
    API.BUDGET_QUARTERLY_DETAIL,
    params,
  );
}

/**
 * 导出部门费用明细数据
 */
async function exportBudgetQuarterlyDetail(params: Recordable<any>) {
  return requestClient.post(API.EXPORT_BUDGET_QUARTERLY_DETAIL, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

export {
  exportBudgetQuarterlyDetail,
  exportDeptBudgetBalance,
  getBudgetQuarterlyDetail,
  queryDeptBudgetBalance,
};
