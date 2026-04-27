# Git 分支管理规范

## 目录

- [概述](#概述)
- [分支类型](#分支类型)
- [工作流程](#工作流程)
- [提交规范](#提交规范)
- [分支保护](#分支保护)
- [最佳实践](#最佳实践)
- [团队协作](#团队协作)
- [工具配置](#工具配置)

## 概述

本文档定义了团队开发中使用的 Git 分支管理策略，基于 Git Flow 工作流，适用于大多数团队开发场景。该策略确保代码的稳定性、可维护性和团队协作效率。

## 分支类型

### 长期分支

#### `master/main` - 生产环境分支

- **用途**: 生产环境代码，始终保持稳定可发布状态
- **来源**: 只接受来自 `release` 或 `hotfix` 分支的合并
- **要求**: 每次合并都应该打标签 (tag)
- **保护**: 启用分支保护，禁止直接推送

#### `develop` - 开发集成分支

- **用途**: 最新的开发进度，功能分支的合并目标
- **来源**: 从 `master` 分支创建，接收 `feature` 分支的合并
- **要求**: 用于日常集成测试
- **保护**: 要求 Pull Request 审查

### 临时分支

#### `feature/*` - 功能开发分支

- **命名格式**: `feature/功能名称` 或 `feature/JIRA-123`
- **来源**: 从 `develop` 分支创建
- **目标**: 完成后合并回 `develop` 并删除
- **生命周期**: 功能开发期间存在

#### `release/*` - 发布准备分支

- **命名格式**: `release/v1.2.0`
- **来源**: 从 `develop` 分支创建
- **用途**: 只进行 bug 修复和发布准备
- **目标**: 完成后合并到 `master` 和 `develop`
- **生命周期**: 发布准备期间存在

#### `hotfix/*` - 紧急修复分支

- **命名格式**: `hotfix/v1.2.1`
- **来源**: 从 `master` 分支创建
- **用途**: 修复生产环境紧急问题
- **目标**: 完成后合并到 `master` 和 `develop`
- **生命周期**: 紧急修复期间存在

## 工作流程

### 功能开发流程

```bash
# 1. 切换到 develop 分支并更新
git checkout develop
git pull origin develop

# 2. 创建功能分支
git checkout -b feature/user-authentication

# 3. 开发功能...
git add .
git commit -m "feat: add user login functionality"

# 4. 推送到远程
git push origin feature/user-authentication

# 5. 创建 Pull Request 到 develop 分支
# 6. 代码审查通过后合并
# 7. 删除功能分支
git branch -d feature/user-authentication
```

### 发布流程

```bash
# 1. 从 develop 创建 release 分支
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# 2. 进行发布准备（版本号更新、文档更新等）
git commit -m "chore: bump version to 1.2.0"

# 3. 合并到 master
git checkout master
git merge release/v1.2.0
git tag v1.2.0
git push origin master --tags

# 4. 合并回 develop
git checkout develop
git merge release/v1.2.0

# 5. 删除 release 分支
git branch -d release/v1.2.0
```

### 紧急修复流程

```bash
# 1. 从 master 创建 hotfix 分支
git checkout master
git pull origin master
git checkout -b hotfix/v1.2.1

# 2. 修复问题
git commit -m "fix: resolve critical security issue"

# 3. 合并到 master
git checkout master
git merge hotfix/v1.2.1
git tag v1.2.1
git push origin master --tags

# 4. 合并回 develop
git checkout develop
git merge hotfix/v1.2.1

# 5. 删除 hotfix 分支
git branch -d hotfix/v1.2.1
```

## 提交规范

采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Type 类型

| 类型 | 说明 | 示例 |
| --- | --- | --- |
| `feat` | 新功能 | `feat: add user authentication system` |
| `fix` | 修复 bug | `fix: resolve login button not responding` |
| `docs` | 文档变更 | `docs: update API documentation` |
| `style` | 代码格式调整（不影响功能） | `style: format code according to style guide` |
| `refactor` | 重构代码 | `refactor: optimize database query performance` |
| `test` | 添加或修改测试 | `test: add unit tests for user service` |
| `chore` | 构建过程或工具变动 | `chore: update dependencies` |
| `perf` | 性能优化 | `perf: improve image loading speed` |
| `ci` | CI/CD 相关变更 | `ci: add GitHub Actions workflow` |
| `build` | 构建系统或外部依赖变更 | `build: update webpack configuration` |

### 提交示例

```bash
# 新功能
feat: add user authentication system
feat(auth): implement OAuth2 login flow

# 修复
fix: resolve login button not responding
fix(api): handle null response from user service

# 文档
docs: update README with installation guide
docs(api): add endpoint documentation

# 重构
refactor: optimize database query performance
refactor(utils): extract common validation logic

# 测试
test: add unit tests for user service
test(integration): add API endpoint tests

# 构建
chore: update dependencies to latest versions
chore: configure ESLint rules
```

## 分支保护

### Master/Main 分支保护规则

- ✅ 启用分支保护
- ✅ 要求 Pull Request 审查
- ✅ 要求状态检查通过（CI/CD）
- ✅ 禁止直接推送
- ✅ 要求分支是最新的
- ✅ 限制可以推送的用户/团队

### Develop 分支保护规则

- ✅ 要求 Pull Request 审查
- ✅ 要求状态检查通过
- ✅ 允许管理员绕过规则
- ✅ 限制可以推送的用户/团队

### 保护规则配置

```yaml
# GitHub 分支保护配置示例
branches:
  - name: master
    protection:
      required_pull_request_reviews:
        required_approving_review_count: 1
        dismiss_stale_reviews: true
      required_status_checks:
        strict: true
        contexts:
          - 'ci/build'
          - 'ci/test'
      enforce_admins: true
      restrictions:
        users: []
        teams: ['developers']
```

## 最佳实践

### 代码审查

- 每个 Pull Request 至少需要 1 人审查
- 审查内容包括：
  - 代码质量和规范性
  - 业务逻辑正确性
  - 安全性考虑
  - 性能影响
- 使用 GitHub/GitLab 的审查功能
- 审查通过后才能合并

### 持续集成

- 每个分支推送时触发 CI
- CI 流程包括：
  - 代码检查（ESLint, Prettier）
  - 单元测试
  - 集成测试
  - 构建验证
- 只有 CI 通过才能合并
- 失败的 CI 需要及时修复

### 版本管理

- 使用语义化版本号 (SemVer)
- 格式：主版本号.次版本号.修订号
- 例如：1.2.3
- 版本号含义：
  - 主版本号：不兼容的 API 修改
  - 次版本号：向下兼容的功能性新增
  - 修订号：向下兼容的问题修正

### 分支命名规范

```bash
# 功能分支
feature/user-profile
feature/payment-integration
feature/PROJ-123

# 修复分支
fix/login-error
fix/memory-leak

# 紧急修复
hotfix/v1.2.1
hotfix/security-patch

# 发布分支
release/v1.2.0
release/v2.0.0-beta
```

## 团队协作

### 合并策略

| 分支类型 | 目标分支       | 合并策略              | 说明                  |
| -------- | -------------- | --------------------- | --------------------- |
| feature  | develop        | Squash and merge      | 保持 develop 历史清晰 |
| release  | master         | Create a merge commit | 保留发布历史          |
| hotfix   | master/develop | Create a merge commit | 保留修复历史          |

### 冲突解决

1. **预防冲突**
   - 及时同步主分支
   - 小批量提交
   - 明确的功能边界

2. **解决冲突**
   - 在本地解决冲突
   - 使用 `git rebase` 保持历史清晰
   - 冲突解决后重新测试

3. **冲突工具**
   - 使用 VS Code 等编辑器的合并工具
   - 使用 `git mergetool` 配置外部工具

### 团队沟通

- 使用 Pull Request 进行代码审查讨论
- 在提交信息中提供足够的上下文
- 及时更新项目文档
- 定期进行代码审查会议

## 工具配置

### Git 别名设置

```bash
# 设置常用别名
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status
git config --global alias.lg "log --oneline --graph --all"
git config --global alias.lga "log --oneline --graph --all --decorate"
git config --global alias.unstage "reset HEAD --"
git config --global alias.last "log -1 HEAD"
```

### 辅助工具

#### Git Flow

```bash
# 安装 Git Flow
npm install -g git-flow

# 初始化 Git Flow
git flow init

# 使用 Git Flow 命令
git flow feature start user-auth
git flow feature finish user-auth
git flow release start v1.2.0
git flow release finish v1.2.0
git flow hotfix start v1.2.1
git flow hotfix finish v1.2.1
```

#### Husky (Git Hooks)

```json
{
  "husky": {
    "hooks": {
      "commit-msg": "commitlint -E HUSKY_GIT_PARAMS",
      "pre-commit": "lint-staged"
    }
  }
}
```

#### Commitizen

```bash
# 安装 Commitizen
npm install -g commitizen

# 配置
echo '{ "path": "cz-conventional-changelog" }' > .czrc

# 使用
git cz
```

#### Conventional Changelog

```bash
# 安装
npm install -g conventional-changelog-cli

# 生成变更日志
conventional-changelog -p angular -i CHANGELOG.md -s
```

### IDE 配置

#### VS Code 扩展推荐

- GitLens
- Git History
- Conventional Commits
- Git Graph

#### 配置示例

```json
{
  "git.enableSmartCommit": true,
  "git.confirmSync": false,
  "git.autofetch": true,
  "git.autofetchPeriod": 180,
  "git.showPushSuccessNotification": true
}
```

## 常见问题

### Q: 如何处理紧急修复？

A: 使用 `hotfix` 分支，从 `master` 创建，修复后合并到 `master` 和 `develop`。

### Q: 功能分支可以合并到 master 吗？

A: 不可以，功能分支只能合并到 `develop` 分支。

### Q: 如何处理长期功能分支？

A: 定期从 `develop` 分支 rebase，避免产生大量冲突。

### Q: 发布前需要做什么准备？

A: 创建 `release` 分支，进行最终测试和文档更新，然后合并到 `master`。

### Q: 如何回滚错误的合并？

A: 使用 `git revert` 创建回滚提交，避免修改历史。

## 总结

这套分支管理方案的核心原则是：

- 保持主分支稳定
- 功能开发隔离
- 发布流程规范化
- 团队协作标准化

根据团队规模和项目需求，可以适当调整具体实施细节，但核心原则应该保持一致。
