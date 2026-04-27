<script lang="ts" setup>
import { computed, watch } from 'vue';

import { AuthenticationLoginExpiredModal, useVbenModal } from '@vben/common-ui';
// import { openWindow } from '@vben/utils';
// import { VBEN_DOC_URL, VBEN_GITHUB_URL } from '@vben/constants';
import { useWatermark } from '@vben/hooks';
// import { BookOpenText, CircleHelp, MdiGithub } from '@vben/icons';
import { BasicLayout, LockScreen, UserDropdown } from '@vben/layouts';
import { preferences } from '@vben/preferences';
import { LOGIN_MODE, useAccessStore, useUserStore } from '@vben/stores';

import { useZoom } from '#/hooks/userZoom';
import { $t } from '#/locales';
import { useAuthStore } from '#/store';
import ChangePasswordForm from '#/views/_core/authentication/change-password.vue';
import LoginForm from '#/views/_core/authentication/login.vue';
import Notification from '#/views/_core/notification/index.vue';

const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { destroyWatermark, updateWatermark } = useWatermark();
const { applyZoom } = useZoom(preferences.app.zoom);
const [ChangePasswordModal, changePasswordModalApi] = useVbenModal({
  connectedComponent: ChangePasswordForm,
  destroyOnClose: true,
});

const menus = computed(() => {
  const isSSO = accessStore.loginMode === LOGIN_MODE.SSO;
  return isSSO
    ? []
    : [
        {
          handler: () => {
            changePasswordModalApi.open();
          },
          icon: 'carbon:password',
          text: $t('common.changePassword'),
        },
      ];
});

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

watch(
  () => preferences.app.watermark,
  async (enable) => {
    if (enable) {
      await updateWatermark({
        content: `${userStore.userInfo?.username} - ${userStore.userInfo?.realName}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);

watch(
  () => accessStore.needChangePassword,
  (needChangePassword) => {
    setTimeout(() => {
      needChangePassword && changePasswordModalApi.open();
    }, 200);
  },
);

watch(
  () => preferences.app.zoom,
  (zoom) => {
    applyZoom(zoom);
  },
);
</script>

<template>
  <BasicLayout @clear-preferences-and-logout="handleLogout">
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :menus
        :text="userStore.userInfo?.displayName"
        :description="userStore.userInfo?.email"
        :tag-text="userStore.userInfo?.post"
        @logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification />
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm :sub-title="$t('common.signUpSubtitle')" />
      </AuthenticationLoginExpiredModal>
      <ChangePasswordModal />
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
</template>
