import { requestClient } from '#/api/request';

export namespace ApiLogApi {
  /** 查询参数 */
  export interface QueryParams {
    pageNo: number;
    pageSize: number;
    documentNo?: string;
  }

  /** 报文日志项 */
  export interface ApiLogItem {
    id: string;
    documentNo: string;
    responseBody: string;
    requestTime?: string;
    apiUrl?: string;
    requestMethod?: string;
    statusCode?: string;
    requestBody?: string;
    duration?: number;
  }

  /** 分页响应 */
  export interface PageResponse {
    list: ApiLogItem[];
    total: number;
  }
}

enum API {
  PAGE = '/budget-api/api-request-log/page',
}

/**
 * 分页查询报文日志
 */
export async function getApiLogPageApi(
  params: ApiLogApi.QueryParams,
): Promise<ApiLogApi.PageResponse> {
  return requestClient.post<ApiLogApi.PageResponse>(API.PAGE, params, {
    // 日志分页在大数据量时后端查询较慢，单独放宽超时
    timeout: 600_000,
  });
}
