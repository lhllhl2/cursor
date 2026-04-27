import { defineOverridesPreferences } from '@vben/preferences';

// import avatar from '#/assets/favicon.ico';
import logo from '#/assets/JA-logo.png';
/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    name: import.meta.env.VITE_APP_TITLE,
    defaultHomePath: '/home',
    // authPageLayout: 'panel-center',
    accessMode: 'backend',
    preferencesButtonPosition: 'header',
    zoom: 100,
    formStyle: 'compact',
    defaultAvatar: '',
  },
  theme: {
    mode: 'light',
    // builtinType: 'green',
    // colorPrimary: 'hsl(161 90% 43%)',
  },
  logo: {
    source: logo,
    enable: true,
    fit: 'contain',
  },
  sidebar: {
    collapsed: true,
    collapsedShowTitle: true,
  },
  widget: {},
});
