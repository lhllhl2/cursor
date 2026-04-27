#!/bin/bash
# 在联网环境（如63节点）构建包含 pnpm 的基础镜像
# 用于离线环境的 Docker 构建

set -e

echo "=== 构建包含 pnpm 的基础镜像 ==="

# 构建基础镜像
docker build -t node:20-alpine-pnpm - <<EOF
FROM node:20-alpine
RUN corepack enable && corepack prepare pnpm@10.12.4 --activate
EOF

echo "=== 基础镜像构建完成 ==="
echo "镜像名称: node:20-alpine-pnpm"

# 导出镜像为 tar 文件
echo "=== 导出镜像为 tar 文件 ==="
docker save -o node-20-alpine-pnpm.tar node:20-alpine-pnpm

echo "=== 镜像导出完成 ==="
echo "文件: node-20-alpine-pnpm.tar"
echo ""
echo "=== 使用说明 ==="
echo "1. 将 node-20-alpine-pnpm.tar 传输到离线服务器"
echo "2. 在离线服务器执行: docker load -i node-20-alpine-pnpm.tar"
echo "3. 修改 Dockerfile 的 FROM 为: FROM node:20-alpine-pnpm"
echo "4. 删除 Dockerfile 中的 'RUN corepack enable' 行（因为基础镜像已包含）"

