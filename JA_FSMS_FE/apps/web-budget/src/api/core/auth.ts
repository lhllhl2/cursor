import { requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数（与后端 LocalLoginVo 对齐：userName + pwd；兼容 username/password） */
  export interface LoginParams {
    userName?: string;
    pwd?: string;
    password?: string;
    username?: string;
  }

  /** 登录接口返回值 */
  export interface LoginResult {
    token: string;
    needChanged: boolean;
  }

  export interface RefreshTokenResult {
    token: string;
    status: number;
  }
}

enum API {
  CALLBACK = '/oauth-api/sso/oauth2/callback',
  CHANGE_PASSWORD = '/admin-api/system/user/updatePwd',
  LOGIN = '/oauth-api/local/login',
  LOGOUT = '/oauth-api/local/logout',
}

/**
/**
 * 刷新accessToken
 */
export async function refreshTokenApi(code?: string) {
  return requestClient.post<AuthApi.RefreshTokenResult>(API.CALLBACK, {
    code,
  });
}

/**
 * 退出登录
 */
export async function logoutApi() {
  return requestClient.post(API.LOGOUT);
}

/**
 * 获取用户权限码
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/auth/codes');
}

/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  const body = {
    userName: data.userName ?? data.username,
    pwd: data.pwd ?? data.password,
  };
  return requestClient.post<AuthApi.LoginResult>(API.LOGIN, body);
}

/**
 * 修改密码（字段与后端 updatePwd 接口一致，非登录接口）
 */
export async function changePasswordApi(data: Record<string, unknown>) {
  return requestClient.post(API.CHANGE_PASSWORD, data);
}
