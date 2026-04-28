import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      preview: {
        allowedHosts: ['finreport.jasolar.com', 'budgetcontrol.jasolar.com'],
      },
      server: {
        // trycloudflare / ngrok 等临时域名；否则 Host 校验会 403
        allowedHosts: true,
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // 默认走本机网关（与线上一致：前端 → jasolar-gateway → jasolar-module-system）。
            // 需同时启动网关 8089 与 system 8081；仅调 system 时可设 VITE_API_PROXY_TARGET=http://127.0.0.1:8081
            // 云/离线无法访问公司域名时勿用默认 https://budgetcontrol...（会代理失败整页 500）。
            // 联公司测试环境：
            //   VITE_API_PROXY_TARGET=https://budgetcontrol.jasolar.com/api pnpm dev:budget
            target:
              process.env.VITE_API_PROXY_TARGET ||
              'http://127.0.0.1:8089',
            ws: true,
          },
        },
      },
    },
  };
});
