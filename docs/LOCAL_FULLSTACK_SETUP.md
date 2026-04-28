# 本地 / Cloud Agent 全栈初始化（Gateway + System + Oracle + Redis + 前端）

本文档汇总本次在 Cursor Cloud Agent 中打通 **jasolar-gateway → jasolar-module-system → JA_FSMS_FE** 所需的依赖、配置、数据库脚本与启动顺序。新环境 **clone 本仓库后按顺序执行** 即可复现。

> **说明**：虚拟机磁盘不会随新 Agent 保留；能随 Git 走的是本仓库里的 **脚本与 YAML**。Oracle 数据需你在本机或容器里 **重新执行 SQL**。

---

## 1. 前置条件

| 组件 | 版本 / 要求 |
|------|----------------|
| JDK | 21（与 `jasolar-parent` 一致即可） |
| Maven | 3.8+ |
| Node.js | 与 `JA_FSMS_FE` 的 `package.json` / engines 一致 |
| pnpm | 仓库使用 `pnpm`（`preinstall` 会限制包管理器） |
| Redis | 7.x，默认 `127.0.0.1:6379`，密码 **`123456`**（与 `application-local-overrides` 一致） |
| Oracle | 本地推荐 **Oracle Database Free**（Docker 镜像 `gvenzl/oracle-free`），服务名 **`FREEPDB1`** |

可选：安装 `cloudflared`（或下载二进制到 `/tmp/cloudflared`）用于公网访问前端 dev server。

---

## 2. 仓库内已落地的配置（无需再手改即可对齐本次行为）

- **System 本地**：`jasolar-module-system/.../application-local.yaml` + `application-local-overrides.yaml`（`jasolar_budget` 用户、Redis URL、`DB_SCHEMA` 等）。
- **Gateway 本地**：`jasolar-gateway/.../application-local.yaml`（直连 `8081`、Feign 静态实例、`/oauth-api/local/**` 路由）；`application.yaml` 中 **`permission.ignoreUrls`** 含本地登录与 `getLangs`。
- **Gateway 端口**：`application.yaml` 使用 **`JASOLAR_GATEWAY_PORT`**，避免环境变量 **`SERVER_PORT=8081`** 把网关绑到与 system 冲突的端口；启动请用 **`jasolar-gateway/scripts/run-local.sh`**。
- **前端**：`JA_FSMS_FE/apps/web-budget/vite.config.mts` 默认代理 **`http://127.0.0.1:8089`**（经网关）；可用 `VITE_API_PROXY_TARGET` 覆盖。
- **Oracle DDL/DML**：`jasolar-module-system/.../scripts/oracle/` 下 `001`～`005`（见下文顺序）。

---

## 3. Oracle 初始化顺序（在 PDB 上以 `jasolar_budget` 或 `system` 执行，按你们习惯）

脚本目录：`jasolar-module-system/jasolar-module-system-biz/scripts/oracle/`

1. **`001_jasolar_budget_tables.sql`** — 创建用户/表空间及基础表。  
2. **`002_system_user_manage_org_project_col.sql`** — `system_user`、`system_manage_org` 等。  
3. **`003_tables_for_seed_import.sql`** — 种子表结构。  
4. **`src/main/resources/sql/`** 下各 `INSERT` 文件 — 导入业务种子数据（按你们当时导入顺序即可）。  
5. **`004_dev_local_login_user.sql`** — 测试用户 **`dev_local` / `DevLocal123!`**（BCrypt）。  
6. **`005_copy_user_group_r_from_user.sql`** — 将 **`GBCS009`** 的 **`system_user_group_r`** 复制到 **`dev_local`**（否则登录后无菜单，易跳 `/home` 404）。

连接串示例：`jdbc:oracle:thin:@127.0.0.1:1521/FREEPDB1`，用户 **`jasolar_budget`** / 密码与 `001` 中一致（本地常用 **`Oracle123`**）。

> **注意**：`ALTER SESSION` 与 PL/SQL 匿名块在部分客户端需 **分开执行**；若遇 **ORA-03405**，不要把 `ALTER SESSION` 与 `DECLARE ... END;` 粘成一条执行。

---

## 4. Docker（Oracle）若遇存储驱动错误

部分环境默认 `overlayfs` 会报 `invalid argument`，可改用 **vfs**（磁盘占用更大，仅适合开发）：

```json
{ "storage-driver": "vfs" }
```

写入 `/etc/docker/daemon.json` 后重启 Docker，再拉取并启动 Oracle Free 容器（具体 `docker run` 参数与你们端口映射保持一致即可）。

---

## 5. Redis

```bash
sudo apt-get update && sudo apt-get install -y redis-server
redis-cli CONFIG SET requirepass 123456
```

与 `application-local-overrides.yaml` 中 `spring.data.redis.url` 一致。

---

## 6. 构建后端 JAR（在仓库根目录）

```bash
./scripts/bootstrap-fullstack.sh --build
```

或手动：

```bash
cd jasolar-gateway && mvn -q package -DskipTests
cd ../jasolar-module-system && mvn -q -pl jasolar-module-system-biz -am package -DskipTests
```

---

## 7. 启动顺序与端口

| 服务 | 端口 | 启动要点 |
|------|------|-----------|
| Redis | 6379 | 见第 5 节 |
| Oracle | 1521 | 容器或本机安装 |
| **jasolar-module-system** | **8081** | `ENV_CONFIG=local`，`java -jar jasolar-module-system-biz/target/jasolar-module-system-biz.jar` |
| **jasolar-gateway** | **8089** | **必须**用 `jasolar-gateway/scripts/run-local.sh`（会 `unset SERVER_PORT`） |
| **JA_FSMS_FE** | **5766**（默认） | `cd JA_FSMS_FE && pnpm install --registry=https://registry.npmjs.org/` 后 `pnpm dev:budget` |

启动 system 示例：

```bash
cd jasolar-module-system/jasolar-module-system-biz
nohup env ENV_CONFIG=local java -jar target/jasolar-module-system-biz.jar > /tmp/jasolar-system.log 2>&1 &
```

启动 gateway 示例：

```bash
cd jasolar-gateway
./scripts/run-local.sh > /tmp/jasolar-gateway.log 2>&1 &
```

---

## 8. 前端与公网隧道

```bash
cd JA_FSMS_FE
pnpm install --registry=https://registry.npmjs.org/
pnpm dev:budget
```

公网（trycloudflare）在 **Vite 已监听** 后再执行：

```bash
./scripts/start-dev-tunnel.sh
```

脚本会读 `VITE_PORT`（默认 5766）、`CLOUDFLARED_BIN`（默认 `/tmp/cloudflared`），并把 URL 打到日志。

---

## 9. 自检命令

```bash
curl -s -o /dev/null -w 'system getLangs: %{http_code}\n' http://127.0.0.1:8081/admin-api/system/i18n-menu/getLangs
curl -s -o /dev/null -w 'gateway getLangs: %{http_code}\n' http://127.0.0.1:8089/admin-api/system/i18n-menu/getLangs
curl -s -o /dev/null -w 'vite proxy: %{http_code}\n' http://127.0.0.1:5766/api/admin-api/system/i18n-menu/getLangs
```

登录（经网关，无 token）：

```bash
curl -s -X POST http://127.0.0.1:8089/oauth-api/local/login \
  -H 'Content-Type: application/json' \
  -d '{"userName":"dev_local","pwd":"DevLocal123!"}'
```

---

## 10. 常见问题

| 现象 | 处理 |
|------|------|
| 网关起不来报 **8081 已被占用** | 父 shell 带了 **`SERVER_PORT=8081`**；请用 **`run-local.sh`** 启动网关。 |
| 前端 **`/api/...` 500** 且响应体为空 | Vite 代理连不上 `8089`（网关未起）或误指外网域名；检查 **`vite.config.mts`** 与 **`VITE_API_PROXY_TARGET`**。 |
| **`getLangs` 500**（后端已通） | Redis 里 **`i18n`** 缓存损坏；`redis-cli -a 123456 DEL i18n` 或已合并的 Controller 容错逻辑。 |
| trycloudflare **502** | **5766 未监听**或 tunnel 指错端口；先本机打开 `http://127.0.0.1:5766/web-budget/`。 |
| 登录后 **404 /home** | 未执行 **`005_copy_user_group_r_from_user.sql`**，`dev_local` 无菜单用户组。 |
| 日志里 **`V_EHR_CONTROL_LEVEL` 不存在** | 测试库视图未导入；不影响本次登录与 `getLangs` 自检，按需补视图或忽略。 |

---

## 11. 测试账号（仅本地种子）

| 用户名 | 密码 | 说明 |
|--------|------|------|
| `dev_local` | `DevLocal123!` | `004` 写入；`005` 与 GBCS009 对齐菜单组 |

**不提供**其他真实用户明文密码；生产/测试账号请走贵司账号体系。

---

## 12. 环境持久化（Cursor Cloud）

Cloud Agent **默认不保留**整台 VM。若需「下次新建 Agent 少装依赖」：

- 使用 Cursor **onboard 后保存 VM 快照**（见官方 **Cloud Agent setup**）；或  
- 在本仓库维护 **`.cursor/environment.json` + Dockerfile`**；或  
- 使用 **My Machines** 固定到自己的机器。

详见 [Cursor Cloud Agent 文档](https://cursor.com/docs/cloud-agent.md)。

---

## 13. 本仓库一键入口

```bash
./scripts/bootstrap-fullstack.sh --help
```

构建 + 打印后续手动步骤（Oracle/Redis 因环境差异不强行全自动）。
