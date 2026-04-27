<script setup lang="ts">
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed } from 'vue';

import { AuthenticationRegister, useVbenModal, z } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { useAccessStore } from '@vben/stores';

import { message } from 'ant-design-vue';

import { changePasswordApi } from '#/api/core/auth';
import { useRequest } from '#/hooks';

interface Props {
  /**
   * @zh_CN 验证码登录路径
   */
  codeLoginPath?: string;
  /**
   * @zh_CN 忘记密码路径
   */
  forgetPasswordPath?: string;

  /**
   * @zh_CN 是否处于加载处理状态
   */
  loading?: boolean;

  /**
   * @zh_CN 二维码登录路径
   */
  qrCodeLoginPath?: string;

  /**
   * @zh_CN 注册路径
   */
  registerPath?: string;

  /**
   * @zh_CN 是否显示验证码登录
   */
  showCodeLogin?: boolean;
  /**
   * @zh_CN 是否显示忘记密码
   */
  showForgetPassword?: boolean;

  /**
   * @zh_CN 是否显示二维码登录
   */
  showQrcodeLogin?: boolean;

  /**
   * @zh_CN 是否显示注册按钮
   */
  showRegister?: boolean;

  /**
   * @zh_CN 是否显示记住账号
   */
  showRememberMe?: boolean;

  /**
   * @zh_CN 是否显示第三方登录
   */
  showThirdPartyLogin?: boolean;

  /**
   * @zh_CN 登录框子标题
   */
  subTitle?: string;

  /**
   * @zh_CN 登录框标题
   */
  title?: string;
  /**
   * @zh_CN 提交按钮文本
   */
  submitButtonText?: string;
  avatar?: string;
  zIndex?: number;
}

defineOptions({
  name: 'ChangePasswordForm',
});

const props = withDefaults(defineProps<Props>(), {
  avatar: '',
  zIndex: 0,
});
const accessStore = useAccessStore();
const changePasswordService = useRequest(changePasswordApi, {
  onSuccess: () => {
    modalApi.close();
    message.success($t('common.changePasswordSuccess'));
    setTimeout(() => {
      accessStore.setLoginExpired(true);
    }, 500);
  },
});

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('common.oldPassword'),
      },
      fieldName: 'pwd',
      label: $t('common.oldPassword'),
      rules: z.string().min(1, { message: $t('common.oldPasswordTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: $t('authentication.password'),
      },
      fieldName: 'newPwd',
      label: $t('authentication.password'),
      renderComponentContent() {
        return {
          strengthText: () => $t('authentication.passwordStrength'),
        };
      },
      rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('authentication.confirmPassword'),
      },
      dependencies: {
        rules(values) {
          const { newPwd } = values;
          return z
            .string({ required_error: $t('authentication.passwordTip') })
            .min(1, { message: $t('authentication.passwordTip') })
            .refine((value) => value === newPwd, {
              message: $t('authentication.confirmPasswordTip'),
            });
        },
        triggerFields: ['newPwd'],
      },
      fieldName: 'confirmPwd',
      label: $t('authentication.confirmPassword'),
    },
  ];
});

const [Modal, modalApi] = useVbenModal();

const getZIndex = computed(() => {
  return props.zIndex || calcZIndex();
});

/**
 * 排除ant-message和loading:9999的z-index
 */
const zIndexExcludeClass = ['ant-message', 'loading'];
function isZIndexExcludeClass(element: Element) {
  return zIndexExcludeClass.some((className) =>
    element.classList.contains(className),
  );
}

/**
 * 获取最大的zIndex值
 */
function calcZIndex() {
  let maxZ = 0;
  const elements = document.querySelectorAll('*');
  [...elements].forEach((element) => {
    const style = window.getComputedStyle(element);
    const zIndex = style.getPropertyValue('z-index');
    if (
      zIndex &&
      !Number.isNaN(Number.parseInt(zIndex)) &&
      !isZIndexExcludeClass(element)
    ) {
      maxZ = Math.max(maxZ, Number.parseInt(zIndex));
    }
  });
  return maxZ + 1;
}

function handleSubmit(value: Recordable<any>) {
  changePasswordService.run(value);
}
</script>

<template>
  <div>
    <Modal
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :footer="false"
      :fullscreen-button="false"
      :header="false"
      :z-index="getZIndex"
      class="border-none px-10 py-6 text-center shadow-xl sm:w-[600px] sm:rounded-2xl md:h-[unset]"
    >
      <AuthenticationRegister
        :title="$t('common.changePassword')"
        :submit-button-text="$t('common.ok')"
        :sub-title="$t('common.changePasswordSubtitle')"
        :form-schema="formSchema"
        :loading="changePasswordService.loading.value"
        :show-go-to-login="false"
        @submit="handleSubmit"
      />
    </Modal>
  </div>
</template>
