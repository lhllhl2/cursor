# 项目结构

## Monorepo 组织方式

这是一个基于 Vue Vben Admin 5.x 架构的 pnpm workspace monorepo 项目。

## 顶层目录

### `/apps`
应用入口。每个应用都是独立可部署的应用程序。

- `web-budget/` - 主要的预算控制应用（Ant Design Vue）
  - `src/api/` - API 服务层
  - `src/views/` - 页面组件
  - `src/router/` - 路由定义
  - `src/store/` - Pinia 状态管理
  - `src/locales/` - 国际化翻译
  - `src/utils/` - 应用特定工具函数
  - `src/adapter/` - 第三方库适配器

### `/packages`
跨应用共享的包。

- `@core/` - 核心框架包
  - `base/` - 基础包（design、icons、shared、typings）
  - `composables/` - Vue 组合式工具
  - `preferences/` - 用户偏好管理
  - `ui-kit/` - UI 组件库（form-ui、layout-ui、menu-ui、popup-ui、shadcn-ui、tabs-ui）

- `effects/` - 副作用相关包
  - `access/` - 访问控制和权限
  - `common-ui/` - 通用 UI 组件
  - `hooks/` - 应用钩子
  - `layouts/` - 布局组件
  - `plugins/` - 第三方插件（echarts、motion、vxe-table）
  - `request/` - HTTP 请求处理

- `constants/` - 共享常量
- `icons/` - 图标管理
- `locales/` - 国际化
- `preferences/` - 偏好设置工具
- `stores/` - 共享 Pinia stores
- `styles/` - 全局样式
- `types/` - 共享 TypeScript 类型
- `utils/` - 共享工具函数

### `/internal`
内部开发工具和配置。

- `lint-configs/` - 代码检查配置
  - `commitlint-config/` - 提交信息规则
  - `eslint-config/` - ESLint 规则
  - `prettier-config/` - 代码格式化
  - `stylelint-config/` - 样式检查

- `node-utils/` - Node.js 工具
- `tailwind-config/` - Tailwind 预设
- `tsconfig/` - TypeScript 配置
- `vite-config/` - Vite 构建配置

### `/scripts`
构建和开发脚本。

- `turbo-run/` - Turbo 执行包装器
- `vsh/` - 自定义 CLI 工具（check-circular、check-dep、lint 等）

### `/k8s`
Kubernetes 部署清单。

- 包含开发和生产环境的 ConfigMaps、Deployments、Services、Ingress

### `/docs`
项目文档。

## 关键约定

### 导入别名

应用使用 `#/*` 进行内部导入：
```typescript
import { something } from '#/utils/common';
```

### 包命名规范

- 应用：`@JA-FSMS-FE/[app-name]`
- 核心包：`@vben-core/[package-name]`
- 其他包：`@vben/[package-name]`

### Workspace 引用

包使用 `workspace:*` 协议引用内部依赖。

### 文件组织

- `.vue` 文件用于组件
- `.ts` 文件用于逻辑
- `.mts` 用于 ESM 配置文件
- `.mjs` 用于 CommonJS 兼容配置
- `index.ts` 作为桶导出

### 配置层级

1. 根配置应用于整个 monorepo
2. Internal 配置提供共享预设
3. 应用/包配置继承 internal 预设

### 构建输出

- `dist/` - 编译输出
- `node_modules/` - 依赖（gitignored）
- `.turbo/` - Turbo 缓存
- 各种缓存文件（`.eslintcache`、`.stylelintcache`）

## 架构模式

### 分层结构

1. **核心层**（`@core`）- 框架基础
2. **效果层**（`effects`）- 业务逻辑和集成
3. **应用层**（`apps`）- 面向用户的应用

### 共享优先原则

通用功能放在 `/packages` 中，由应用导入使用。避免重复代码。

### 适配器模式

第三方库通过适配器封装（参见 `apps/web-budget/src/adapter/`），隔离集成逻辑。

### 基于功能的组织

在应用内部，代码按功能域（import、report、system）组织，而非技术角色。
