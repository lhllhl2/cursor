/**
 * 该文件可自行根据业务逻辑进行调整
 */
import type { RequestClientOptions } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import {
  authenticateResponseInterceptor,
  defaultResponseInterceptor,
  errorMessageResponseInterceptor,
  RequestClient,
} from '@vben/request';
import { LOGIN_MODE, useAccessStore } from '@vben/stores';

import { message } from 'ant-design-vue';

import { useAuthStore } from '#/store';
import { param2Obj } from '#/utils';
import { buildUnifiedAuthUrl, getCurrentFullPath } from '#/utils/unified-auth';

import { refreshTokenApi } from './core';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

function createRequestClient(baseURL: string, options?: RequestClientOptions) {
  const client = new RequestClient({
    ...options,
    baseURL,
  });

  /**
   * 统一认证逻辑
   * 当token过期或无效时，从响应头中获取统一认证地址进行跳转
   */
  async function doReAuthenticate(response: any) {
    console.warn(
      'Access token or refresh token is invalid or expired, redirecting to unified authentication center.',
    );
    const { headers } = response;
    const accessStore = useAccessStore();
    const authStore = useAuthStore();

    // 清除当前token
    accessStore.setAccessToken(null);
    if (accessStore.loginMode === LOGIN_MODE.SSO) {
      // 从响应头中获取统一认证地址
      const authLocation = headers?.location || headers?.Location;
      const currentPath = getCurrentFullPath();

      // 构建完整的认证URL，包含回调地址
      const authUrl = buildUnifiedAuthUrl(authLocation, {
        redirect_url: currentPath,
        state: currentPath,
      });

      // 跳转到统一认证中心
      window.location.href = authUrl;
    } else {
      if (
        preferences.app.loginExpiredMode === 'modal' &&
        accessStore.isAccessChecked
      ) {
        accessStore.setLoginExpired(true);
      } else {
        await authStore.logoutLocal();
      }
    }
  }

  /**
   * 刷新token逻辑
   */
  async function doRefreshToken() {
    const { href } = window.location;
    const params = param2Obj(href);
    const accessStore = useAccessStore();
    const resp = await refreshTokenApi(params?.code);
    const newToken = resp?.token;
    accessStore.setAccessToken(newToken);
    return newToken;
  }

  function formatToken(token: null | string) {
    return token ? `${token}` : null;
  }

  // 请求头处理
  client.addRequestInterceptor({
    fulfilled: async (config) => {
      const accessStore = useAccessStore();

      config.headers.Authorization = formatToken(accessStore.accessToken);
      config.headers['Accept-Language'] = preferences.app.locale;
      return config;
    },
  });

  // 处理返回的响应数据格式
  client.addResponseInterceptor(
    defaultResponseInterceptor({
      codeField: 'code',
      dataField: 'data',
      successCode: '0',
    }),
  );

  // token过期的处理
  client.addResponseInterceptor(
    authenticateResponseInterceptor({
      client,
      doReAuthenticate,
      doRefreshToken,
      enableRefreshToken: preferences.app.enableRefreshToken,
      formatToken,
    }),
  );

  // 通用的错误处理,如果没有进入上面的错误处理逻辑，就会进入这里
  client.addResponseInterceptor(
    errorMessageResponseInterceptor((msg: string, error) => {
      // 这里可以根据业务进行定制,你可以拿到 error 内的信息进行定制化处理，根据不同的 code 做不同的提示，而不是直接使用 message.error 提示 msg
      // 当前mock接口返回的错误字段是 error 或者 message
      const responseData = error?.response?.data ?? {};
      const errorMessage = responseData?.error ?? responseData?.msg ?? '';
      // 如果没有错误信息，则会根据状态码进行提示
      message.error(errorMessage || msg);
    }),
  );

  return client;
}

export const requestClient = createRequestClient(apiURL, {
  timeout: 30_000,
  responseReturn: 'data',
});

export const baseRequestClient = new RequestClient({ baseURL: apiURL });
