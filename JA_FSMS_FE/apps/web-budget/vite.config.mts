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
            target: 'https://budgetcontrol.jasolar.com/api', // 测试环境
            ws: true,
          },
        },
      },
    },
  };
});
