import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      preview: {
        allowedHosts: ['finreport.jasolar.com', 'budgetcontrol.jasolar.com'],
      },
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // 默认测试环境。本地全链路（前端 → gateway → system）：
            //   VITE_API_PROXY_TARGET=http://127.0.0.1:8089 pnpm dev:budget
            // 仅直连 system（调接口、不测网关）：
            //   VITE_API_PROXY_TARGET=http://127.0.0.1:8081 pnpm dev:budget
            target:
              process.env.VITE_API_PROXY_TARGET ||
              'https://budgetcontrol.jasolar.com/api',
            ws: true,
          },
        },
      },
    },
  };
});
