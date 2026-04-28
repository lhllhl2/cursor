import type { PluginOption } from 'vite';

import { defineConfig } from '@vben/vite-config';

const devI18nMenuMockPath = '/api/admin-api/system/i18n-menu/getLangs';
const devCurrentUserMockPath = '/api/admin-api/system/user/currentUserInfo';

const devMockResponses: Record<string, unknown> = {
  [devCurrentUserMockPath]: {
    code: '0',
    data: {
      avatar: '',
      buttonList: [],
      desc: 'Local development user',
      displayName: '本地开发用户',
      homePath: '/home',
      menuList: [
        {
          component: '/_core/home/index',
          meta: {
            affixTab: true,
            icon: 'lucide:house',
            order: 0,
            title: 'common.home',
          },
          name: 'Home',
          path: '/home',
        },
      ],
      realName: '本地开发用户',
      roles: ['super'],
      token: 'local-dev-token',
      userId: 'local-dev',
      username: 'local-dev',
    },
    msg: '',
  },
  [devI18nMenuMockPath]: {
    code: '0',
    data: [
      {
        id: 'local-common',
        jsonData: {
          goodAfternoon: {
            'en-US': 'Good afternoon',
            'zh-CN': '下午好',
          },
          goodEvening: {
            'en-US': 'Good evening',
            'zh-CN': '晚上好',
          },
          goodMorning: {
            'en-US': 'Good morning',
            'zh-CN': '早上好',
          },
          home: {
            'en-US': 'Home',
            'zh-CN': '首页',
          },
          night: {
            'en-US': 'Good night',
            'zh-CN': '晚安',
          },
        },
        name: 'common',
        pid: 0,
        title: 'Common',
      },
      {
        id: 'local-home',
        jsonData: {
          description: {
            'en-US': 'Budget control platform local development environment.',
            'zh-CN': '预算控制中台本地开发环境。',
          },
          quickNavDescription1: {
            'en-US': 'Open the budget planning workspace.',
            'zh-CN': '打开预算编制工作台。',
          },
          quickNavDescription2: {
            'en-US': 'View department budget balance reports.',
            'zh-CN': '查看部门预算余额报表。',
          },
          quickNavTitle1: {
            'en-US': 'Budget Planning',
            'zh-CN': '预算编制',
          },
          quickNavTitle2: {
            'en-US': 'Budget Balance',
            'zh-CN': '预算余额',
          },
          welcome: {
            'en-US': 'Welcome to the budget control platform',
            'zh-CN': '欢迎使用预算控制中台',
          },
        },
        name: 'home',
        pid: 0,
        title: 'Home',
      },
    ],
    msg: '',
  },
};

function createDevBackendBootstrapMockPlugin(enabled: boolean): PluginOption {
  return {
    configureServer(server) {
      if (!enabled) {
        return;
      }

      server.middlewares.use((req, res, next) => {
        const requestPath = req.url?.split('?')[0];
        const mockResponse = requestPath ? devMockResponses[requestPath] : null;

        if (!mockResponse) {
          next();
          return;
        }

        res.statusCode = 200;
        res.setHeader('Content-Type', 'application/json; charset=utf-8');
        res.end(JSON.stringify(mockResponse));
      });
    },
    enforce: 'pre',
    name: 'web-budget-dev-backend-bootstrap-mock',
  };
}

export default defineConfig(async ({ command, mode }) => {
  const enableDevBackendBootstrapMock =
    command === 'serve' && mode === 'development';

  return {
    application: {},
    vite: {
      plugins: [
        createDevBackendBootstrapMockPlugin(enableDevBackendBootstrapMock),
      ],
      preview: {
        allowedHosts: ['finreport.jasolar.com', 'budgetcontrol.jasolar.com'],
      },
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            target: 'https://budgetcontrol.jasolar.com/api', // 测试环境
            ws: true,
          },
        },
      },
    },
  };
});
