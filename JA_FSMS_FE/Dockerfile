# 使用预构建的基础镜像（包含 pnpm，适用于离线环境）
FROM node:20-alpine-pnpm

# 设置环境变量
ENV NODE_ENV=production
ENV NODE_OPTIONS=--max-old-space-size=8192

# 禁用网络请求（避免在离线环境卡住）
ENV TURBO_TELEMETRY_DISABLED=1
ENV TURBO_TELEMETRY_OPTOUT=1
ENV PNPM_DISABLE_UPDATE_CHECK=1
ENV CI=true

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 强制清理旧的构建文件
RUN rm -rf apps/web-budget/dist apps/web-budget/.turbo

# 清理、安装依赖并构建（合并所有步骤，避免层间卡住）
RUN echo "========== 开始构建流程 ==========" && \
    echo "[1/3] 清理和安装依赖..." && \
    pnpm clean && \
    pnpm install --frozen-lockfile && \
    echo "[1/3] ✓ 依赖安装完成" && \
    echo "" && \
    echo "[2/3] 开始构建项目..." && \
    echo "执行: pnpm build:budget -- --mode production" && \
    pnpm build:budget -- --mode production && \
    echo "[2/3] ✓ 构建完成" && \
    echo "" && \
    echo "[3/3] 验证构建输出..." && \
    test -d /app/apps/web-budget/dist && \
    echo "[3/3] ✓ dist 目录存在，构建成功" || \
    (echo "[3/3] ✗ dist 目录不存在，构建失败" && exit 1) && \
    echo "========== 构建流程完成 ==========" && \
    echo "等待所有进程结束..." && \
    ps aux | grep -E "(pnpm|node|turbo)" | grep -v grep || echo "无相关进程" && \
    sync && \
    sleep 1 && \
    echo "RUN 步骤完全结束" && \
    echo "=== 测试：在同一 RUN 中执行后续操作 ===" && \
    echo "当前时间: $(date)" && \
    echo "工作目录: $(pwd)" && \
    ls -la /app/apps/web-budget/dist | head -5 && \
    echo "=== 测试完成 ==="

# 暴露端口
EXPOSE 4173

# 启动命令
WORKDIR /app/apps/web-budget
CMD ["pnpm", "preview"] 
