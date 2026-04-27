# 技术栈

## 框架与核心库

- **Vue 3.5+** - 前端框架（Composition API）
- **TypeScript 5.8+** - 类型安全开发
- **Vue Router 4** - 客户端路由
- **Pinia 3** - 状态管理
- **Vite 6** - 构建工具和开发服务器

## UI 框架

- **Ant Design Vue 4** - 主要组件库
- **VXE Table 4** - 高级数据表格
- **Radix Vue** - 无头 UI 组件
- **Tailwind CSS 3** - 实用优先的样式框架
- **Lucide Vue** - 图标库

## 关键依赖

- **Axios** - HTTP 客户端
- **Day.js** - 日期处理
- **VueUse** - Vue 组合式工具集
- **Vee-Validate + Zod** - 表单验证
- **Vue i18n** - 国际化
- **ECharts 5** - 数据可视化

## 构建系统

- **pnpm** (v9.12.0+) - 包管理器（workspace 模式）
- **Turbo** - Monorepo 构建编排
- **Unbuild** - 库打包工具
- **Node.js 20.10.0+** - 运行时要求

## 代码质量工具

- **ESLint 9** - 代码检查（flat config）
- **Prettier 3** - 代码格式化
- **Stylelint 16** - CSS/SCSS 检查
- **Commitlint** - 提交信息验证
- **Lefthook** - Git 钩子
- **CSpell** - 拼写检查
- **Vitest** - 单元测试
- **Playwright** - E2E 测试

## 常用命令

### 开发
```bash
# 启动所有应用（开发模式）
pnpm dev

# 启动特定应用
pnpm dev:budget
pnpm dev:web

# 预览生产构建
pnpm preview
```

### 构建
```bash
# 构建所有应用
pnpm build

# 构建特定应用
pnpm build:budget
pnpm build:web

# 构建并分析包体积
pnpm build:analyze

# 构建 Docker 镜像
pnpm build:docker
```

### 代码质量
```bash
# 运行所有检查
pnpm check

# 类型检查
pnpm check:type

# 代码检查
pnpm lint

# 代码格式化
pnpm format

# 检查循环依赖
pnpm check:circular

# 检查依赖问题
pnpm check:dep

# 拼写检查
pnpm check:cspell
```

### 测试
```bash
# 运行单元测试
pnpm test:unit

# 运行 E2E 测试
pnpm test:e2e
```

### 维护
```bash
# 清理构建产物
pnpm clean

# 重新安装依赖
pnpm reinstall

# 更新依赖
pnpm update:deps

# 使用规范化提交
pnpm commit
```

## 环境要求

- Node.js >= 20.10.0
- pnpm >= 9.12.0
- 必须使用 `pnpm`（preinstall 钩子强制执行）

## 配置文件

- `vite.config.mts` - Vite 配置（使用 @vben/vite-config）
- `tsconfig.json` - TypeScript 配置（继承 @vben/tsconfig）
- `tailwind.config.mjs` - Tailwind 配置
- `eslint.config.mjs` - ESLint flat config
- `.prettierrc.mjs` - Prettier 配置
- `turbo.json` - Turbo 构建流水线
- `pnpm-workspace.yaml` - Workspace 包配置
