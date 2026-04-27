import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

/** 预算类型枚举 */
export enum BudgetType {
  /** 付款额 */
  PAYMENT = 'PAYMENT',
  /** 投资额 */
  TOTALINVESTM = 'TOTALINVESTMENT',
}

export namespace ProjectBalanceApi {
  /** 预算金额对象 */
  export interface BudgetAmount {
    amountTotal: number;
    /** 以前年度已使用金额 */
    lastYearUsedBudget?: number;
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
    budgetType: BudgetType;
    year?: string;
    ehrCode?: string;
    ehrName?: string;
    prjCode?: string;
    prjName?: string;
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
  EXPORT_PROJECT_BUDGET_BALANCE = '/budget-api/query/exportProjectBudgetBalance',
  QUERY_PROJECT_BUDGET_BALANCE = '/budget-api/query/queryProjectBudgetBalance',
}

/**
 * 查询项目可用预算数据
 */
async function queryProjectBudgetBalance(
  params: ProjectBalanceApi.QueryParams,
) {
  return requestClient.post<ProjectBalanceApi.PageResponse>(
    API.QUERY_PROJECT_BUDGET_BALANCE,
    params,
  );
}

/**
 * 导出项目可用预算数据
 */
async function exportProjectBudgetBalance(params: Recordable<any>) {
  return requestClient.post(API.EXPORT_PROJECT_BUDGET_BALANCE, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

export { exportProjectBudgetBalance, queryProjectBudgetBalance };
