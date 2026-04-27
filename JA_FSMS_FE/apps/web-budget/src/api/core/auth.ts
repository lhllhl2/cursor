import { requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
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
  return requestClient.post<AuthApi.LoginResult>(API.LOGIN, data);
}

/**
 * 修改密码
 */
export async function changePasswordApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginParams>(API.CHANGE_PASSWORD, data);
}
