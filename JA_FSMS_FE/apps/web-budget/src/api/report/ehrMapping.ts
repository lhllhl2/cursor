import { requestClient } from '#/api/request';

export namespace EhrMappingApi {
  /** 查询参数 */
  export interface QueryParams {
    pageNo: number;
    pageSize: number;
    ehrCd?: string;
    erpDepart?: string;
    budgetOrgCd?: string;
    budgetEhrCd?: string;
    controlEhrCd?: string;
    ehrNm?: string;
    budgetOrgNm?: string;
    budgetEhrNm?: string;
    controlEhrNm?: string;
  }

  /** 组织映射数据项 */
  export interface EhrMappingItem {
    id: string;
    ehrCd: string;
    erpDepart: string;
    budgetOrgCd: string;
    budgetEhrCd: string;
    controlEhrCd: string;
  }

  /** 列表响应 */
  export interface ListResult {
    list: EhrMappingItem[];
    total: number;
  }
}

enum API {
  EXPORT = '/budget-api/query/exportEhrControlLevel',
  LIST = '/budget-api/query/queryEhrControlLevel',
}

/**
 * 获取组织映射列表
 */
export async function getEhrMappingListApi(params: EhrMappingApi.QueryParams) {
  return requestClient.post<EhrMappingApi.ListResult>(API.LIST, params);
}

/**
 * 导出
 */
export async function exportEhrMapping(params: any) {
  return requestClient.post(API.EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}
