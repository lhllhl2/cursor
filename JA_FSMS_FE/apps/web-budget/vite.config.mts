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
  [devI18nMenuMockPath]: { code: '0', data: [], msg: '' },
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

export default defineConfig(async ({ mode }) => {
  const enableDevBackendBootstrapMock = mode === 'development';

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
