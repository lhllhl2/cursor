#!/usr/bin/env bash
# 一键：确保本机 Vite(5766) 已启动后，拉起 trycloudflare 快速隧道并把公网 URL 写入日志。
# 与 system(8081)、gateway(8089) 无关；隧道只连前端 dev server。
set -euo pipefail
LOG="${CF_TUNNEL_LOG:-/tmp/cloudflared-tunnel.log}"
PORT="${VITE_PORT:-5766}"
CF_BIN="${CLOUDFLARED_BIN:-/tmp/cloudflared}"

if ! ss -tlnp 2>/dev/null | grep -q ":${PORT}"; then
  echo "错误: 127.0.0.1:${PORT} 无监听。请先在本机运行: cd JA_FSMS_FE && pnpm dev:budget" >&2
  exit 1
fi

pkill -f "${CF_BIN} tunnel --url" 2>/dev/null || true
sleep 1
: > "$LOG"
nohup "$CF_BIN" tunnel --url "http://127.0.0.1:${PORT}" >> "$LOG" 2>&1 &
echo "cloudflared 已后台启动，日志: $LOG"
for i in $(seq 1 20); do
  URL=$(grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' "$LOG" | head -1 || true)
  if [[ -n "${URL:-}" ]]; then
    echo "公网访问地址: ${URL}/web-budget/auth/login"
    exit 0
  fi
  sleep 1
done
echo "未在 20s 内从日志解析到 trycloudflare URL，请查看: $LOG" >&2
exit 2
