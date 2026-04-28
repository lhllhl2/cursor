#!/usr/bin/env bash
# 一键：构建 gateway + system-biz JAR，并打印后续初始化说明。
# 用法见 ./scripts/bootstrap-fullstack.sh --help
# 详细文档: docs/LOCAL_FULLSTACK_SETUP.md

set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

usage() {
  cat <<'EOF'
bootstrap-fullstack.sh — 构建后端 JAR 并提示全栈初始化步骤（详见 docs/LOCAL_FULLSTACK_SETUP.md）。

用法:
EOF
  cat <<EOF
  $0 [--build] [--check] [--help]

  --build   执行 Maven 打包（gateway + system-biz）
  --check   仅检测 JDK/mvn/node/pnpm/redis 端口等，不构建
  --help    显示帮助

示例:
  cd "$ROOT" && ./scripts/bootstrap-fullstack.sh --build
EOF
}

DO_BUILD=false
DO_CHECK=false
for a in "$@"; do
  case "$a" in
    --build) DO_BUILD=true ;;
    --check) DO_CHECK=true ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $a" >&2; usage >&2; exit 1 ;;
  esac
done

if ! $DO_BUILD && ! $DO_CHECK; then
  usage
  exit 0
fi

echo "仓库根: $ROOT"

if $DO_CHECK; then
  command -v java >/dev/null && java -version 2>&1 | head -1 || echo "WARN: java 未找到"
  command -v mvn >/dev/null && echo "mvn: $(mvn -version | head -1)" || echo "WARN: mvn 未找到"
  command -v node >/dev/null && echo "node: $(node -v)" || echo "WARN: node 未找到"
  command -v pnpm >/dev/null && echo "pnpm: $(pnpm -v)" || echo "WARN: pnpm 未找到"
  if command -v redis-cli >/dev/null; then
    redis-cli -a 123456 PING 2>/dev/null && echo "redis: PONG (auth ok)" || echo "WARN: redis 6379 或密码与 123456 不一致"
  else
    echo "WARN: redis-cli 未找到"
  fi
  if command -v ss >/dev/null; then
    ss -tlnp 2>/dev/null | grep -E ':1521|:6379|:8081|:8089|:5766' || true
  fi
fi

if $DO_BUILD; then
  echo ">>> Maven: jasolar-gateway"
  (cd "$ROOT/jasolar-gateway" && mvn -q package -DskipTests)
  echo ">>> Maven: jasolar-module-system-biz (+ deps)"
  (cd "$ROOT/jasolar-module-system" && mvn -q -pl jasolar-module-system-biz -am package -DskipTests)
  echo "构建完成:"
  ls -la "$ROOT/jasolar-gateway/target/jasolar-gateway.jar"
  ls -la "$ROOT/jasolar-module-system/jasolar-module-system-biz/target/jasolar-module-system-biz.jar"
fi

cat <<MSG

下一步请阅读: $ROOT/docs/LOCAL_FULLSTACK_SETUP.md

摘要:
  1) Oracle: 按 scripts/oracle/001 → 003，导入 sql/ 种子，再 004、005
  2) Redis: requirepass 123456
  3) 启动 system: ENV_CONFIG=local, jar 见 jasolar-module-system-biz/target/
  4) 启动 gateway: cd jasolar-gateway && ./scripts/run-local.sh
  5) 前端: cd JA_FSMS_FE && pnpm install --registry=https://registry.npmjs.org/ && pnpm dev:budget
  6) 隧道(可选): JA_FSMS_FE/scripts/start-dev-tunnel.sh
MSG
