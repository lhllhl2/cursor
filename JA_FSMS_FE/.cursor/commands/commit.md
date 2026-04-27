# 提交代码

## 概述

按照 Conventional Commits 规范提交代码，自动检查代码质量并生成规范的提交信息。

## 提交类型说明

根据项目配置，支持以下提交类型：

- **feat**: 新增功能
- **fix**: 修复缺陷
- **perf**: 性能优化
- **style**: 代码格式（不影响代码运行的变动）
- **docs**: 文档变更
- **test**: 添加或修改测试
- **refactor**: 代码重构（既不是新增功能，也不是修复缺陷）
- **build**: 构建流程、外部依赖变更
- **ci**: 修改 CI 配置、脚本
- **chore**: 对构建过程或辅助工具和库的更改
- **revert**: 回滚 commit
- **types**: 类型定义文件修改
- **release**: 发布版本

## 提交范围 (scope)

根据项目配置，支持以下范围：

- 包名（如 @vben/ui-kit, @vben/composables 等）
- project, style, lint, ci, dev, deploy, other

## 提交步骤

1. **检查代码状态**
   - 查看当前修改的文件
   - 确认需要提交的文件

2. **代码质量检查**
   - 确保代码已通过 ESLint 检查
   - 确保代码已通过 Prettier 格式化
   - 确保代码已通过 Stylelint 检查（如适用）

3. **生成提交信息**
   - 根据修改内容选择合适的类型（type）
   - 确定合适的范围（scope），如果用户没有指定，根据修改的文件自动推断
   - 编写简洁明确的描述（subject），不超过 108 个字符
   - 如需详细说明，可在 body 中补充

4. **提交格式**
   - <type>[<scope>]: <subject>
   - [可选的详细描述]
   - [可选的脚注，如关联的 issue]

## 提交示例

- `feat(web-budget): 添加组织导入功能`
- `fix(@vben/ui-kit): 修复表格分页显示问题`
- `refactor(api): 重构用户认证逻辑`
- `docs: 更新 README 文档`
- `style: 格式化代码，统一缩进`

## 注意事项

- 提交信息必须符合 Conventional Commits 规范
- 提交前会自动运行 lefthook pre-commit hooks（代码格式化、lint 检查）
- 提交时会自动运行 commitlint 验证提交信息格式
- 如果提交信息不符合规范，commitlint 会阻止提交并提示错误

## 执行流程

1. 分析用户提供的上下文或当前修改的文件
2. 根据修改内容智能推断提交类型和范围
3. 生成符合规范的提交信息
4. 执行 `git add` 添加文件
5. 执行 `git commit` 提交代码
6. 如果提交失败，根据错误信息调整并重试
