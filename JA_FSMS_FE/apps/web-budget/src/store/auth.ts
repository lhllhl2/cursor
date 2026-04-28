import type { UserInfo as BasicUserInfo, Recordable } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import {
  LOGIN_MODE,
  resetAllStores,
  useAccessStore,
  useUserStore,
} from '@vben/stores';

import { notification } from 'ant-design-vue';
import { defineStore } from 'pinia';

import { getUserInfoApi, loginApi, logoutApi } from '#/api';
import { $t } from '#/locales';

interface UserInfo extends BasicUserInfo {
  menuList?: {
    id: string;
    name: string;
    path: string;
    pid: string;
    sort: number;
  }[];
  buttonList?: {
    authCode: string;
  }[];
}

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const loginLoading = ref(false);

  /**
   * 异步处理登录操作
   * Asynchronously handle the login process
   * @param params 登录表单数据
   */
  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    // 异步处理用户登录操作并获取 accessToken
    let userInfo: null | UserInfo = null;
    try {
      loginLoading.value = true;
      const { token, needChanged } = await loginApi(params);

      // 如果成功获取到 accessToken
      if (token) {
        accessStore.setLoginMode(LOGIN_MODE.LOCAL);
        accessStore.setAccessToken(token);

        // 获取用户信息并存储到 accessStore 中
        const fetchUserInfoResult = await fetchUserInfo();

        userInfo = fetchUserInfoResult;

        userStore.setUserInfo(userInfo);
        // accessStore.setAccessCodes(accessCodes);

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else {
          onSuccess
            ? await onSuccess?.()
            : await router.push(
                userInfo?.homePath || preferences.app.defaultHomePath,
              );
        }
        if (needChanged) {
          accessStore.setNeedChangePassword(true);
        }
        if (userInfo?.displayName) {
          notification.success({
            description: `${$t('authentication.loginSuccessDesc')}:${userInfo?.displayName}`,
            duration: 3,
            message: $t('authentication.loginSuccess'),
          });
        }
      }
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    try {
      await logoutApi();
    } catch {
      // 不做任何处理
    }
    resetAllStores();
    accessStore.setLoginExpired(false);

    // 回登录页带上当前路由地址
    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
  }

  function logoutLocal() {
    resetAllStores();
    accessStore.setLoginExpired(false);
    router.replace({
      path: LOGIN_PATH,
    });
  }

  async function fetchUserInfo() {
    const userInfo = (await getUserInfoApi()) as null | UserInfo;
    userStore.setUserInfo(userInfo);
    // 设置权限码
    if (userInfo?.buttonList?.length) {
      const accessCodes = userInfo.buttonList.map((item: any) => item.authCode);
      accessStore.setAccessCodes(accessCodes);
    }
    return userInfo;
  }

  function $reset() {
    loginLoading.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
    logoutLocal,
  };
});
