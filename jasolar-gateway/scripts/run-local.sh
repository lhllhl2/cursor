#!/usr/bin/env bash
# 本地启动网关。父 shell 若带 SERVER_PORT=8081（常见于与 system 同机调试），
# Spring Boot 会用它覆盖 server.port，导致网关抢 system 的 8081。此处显式取消。
set -euo pipefail
cd "$(dirname "$0")/.."
unset SERVER_PORT
exec env ENV_CONFIG=local JASOLAR_GATEWAY_PORT="${JASOLAR_GATEWAY_PORT:-8089}" \
  java -jar target/jasolar-gateway.jar "$@"
