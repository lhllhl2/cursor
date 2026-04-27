import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemLogApi {
  // 用户登录日志数据结构
  export interface UserLoginLog {
    id: string;
    userName: string; // 工号
    displayName: string; // 姓名
    ip: string; // IP地址
    logType: string; // 类型
    createTime: string; // 时间
  }

  // API响应数据结构
  export interface LogPageResponse {
    code: string;
    data: {
      list: UserLoginLog[];
      total: number;
    };

    msg?: string;
    traceId?: string;
  }
}

enum API {
  // 用户登录日志接口
  USER_LOGIN_LOG_PAGE = '/admin-api/system/log/logPage',
}

/**
 * 获取用户登录日志分页数据
 */
async function getUserLoginLogPage(params: Recordable<any>) {
  return requestClient.post<SystemLogApi.LogPageResponse>(
    API.USER_LOGIN_LOG_PAGE,
    params,
  );
}

export { getUserLoginLogPage };
