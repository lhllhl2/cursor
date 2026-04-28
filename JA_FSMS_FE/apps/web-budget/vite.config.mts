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
            // 默认走本机 system，避免云/离线环境无法访问 budgetcontrol.jasolar.com 时
            // Vite 代理连接失败整页返回 500（浏览器看到 /api/... 500、响应体多为空 text/plain）。
            // 需要联公司测试环境或生产 API 时显式设置：
            //   VITE_API_PROXY_TARGET=https://budgetcontrol.jasolar.com/api pnpm dev:budget
            // 本地全链路（前端 → gateway → system）：
            //   VITE_API_PROXY_TARGET=http://127.0.0.1:8089 pnpm dev:budget
            target:
              process.env.VITE_API_PROXY_TARGET ||
              'http://127.0.0.1:8081',
            ws: true,
          },
        },
      },
    },
  };
});
