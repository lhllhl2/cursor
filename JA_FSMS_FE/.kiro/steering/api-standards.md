# API 开发规范

## 文件组织

### 目录结构

```
apps/web-budget/src/api/
├── request.ts          # 请求客户端配置
├── upload.ts           # 文件上传封装
├── index.ts            # 统一导出
├── core/               # 核心功能（认证、用户、菜单）
├── system/             # 系统管理
├── report/             # 报表相关
└── import/             # 数据导入
```

### 模块组织

- 按**功能域**划分目录（如 `core`、`system`、`report`）
- 每个模块包含 `index.ts` 作为桶导出
- 相关 API 放在同一文件中（如 `auth.ts` 包含所有认证相关接口）

## 命名规范

### API 函数命名

所有 API 函数必须以 `Api` 结尾：

```typescript
// ✅ 正确
export async function getUserInfoApi() { }
export async function loginApi(data: AuthApi.LoginParams) { }
export async function getReportListApi(params: QueryParams) { }

// ❌ 错误
export async function getUserInfo() { }
export async function login(data: any) { }
```

### 路径命名

- 路径通过业务英文命名，无统一前缀规范
- 常见前缀：`/admin-api`（管理接口）、`/oauth-api`（认证）
- 使用 `enum API` 定义所有路径常量

```typescript
enum API {
  LOGIN = '/oauth-api/local/login',
  LOGOUT = '/oauth-api/local/logout',
  USER_INFO = '/admin-api/system/user/currentUserInfo',
  REPORT_LIST = '/report-api/payment/list',
}
```

## 类型定义

### 使用 Namespace 组织类型

相关的请求/响应类型使用 namespace 组织：

```typescript
export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    password?: string;
    username?: string;
  }

  /** 登录接口返回值 */
  export interface LoginResult {
    token: string;
    needChanged: boolean;
  }
}
```

### 类型注释

- 使用简单的 JSDoc 注释描述类型用途
- 不需要完整的 `@param`、`@returns` 等标签

```typescript
/**
 * 获取用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/admin-api/system/user/currentUserInfo');
}
```

## 请求方法

### 导入请求客户端

```typescript
import { requestClient } from '#/api/request';
```

### GET 请求

```typescript
// 无参数
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/admin-api/system/user/info');
}

// 带查询参数（遵循 Axios 风格）
export async function getUserListApi(params: QueryParams) {
  return requestClient.get<UserListResult>('/admin-api/system/user/list', {
    params,
  });
}
```

### POST 请求

```typescript
// JSON 格式请求体
export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>(API.LOGIN, data);
}

// 无请求体
export async function logoutApi() {
  return requestClient.post(API.LOGOUT);
}
```

### 文件上传

使用封装的 `uploadFile` 函数：

```typescript
import { uploadFile } from '#/api/upload';

// 在组件中使用
uploadFile({
  url: '/admin-api/system/file/upload',
  file: fileObject,
  onProgress: ({ percent }) => {
    console.log(`上传进度: ${percent}%`);
  },
  onSuccess: (data) => {
    console.log('上传成功', data);
  },
});
```

## 响应格式

### 统一响应结构

```typescript
{
  code: '0',        // 成功码为字符串 '0'
  data: any,        // 业务数据
  msg: string       // 提示信息
}
```

### 分页响应

```typescript
interface PageResult<T> {
  list: T[];        // 数据列表
  total: number;    // 总记录数
}

// 分页请求参数
interface PageParams {
  pageNo: number;   // 页码（从 1 开始）
  pageSize: number; // 每页条数
}
```

### 错误码映射

- 成功码：`'0'`（字符串）
- 错误统一通过 `message.error` 显示
- 特定错误码需要自行处理（如 401 跳转登录、403 权限提示）

## 完整示例

```typescript
import { requestClient } from '#/api/request';

// 定义类型
export namespace ReportApi {
  /** 查询参数 */
  export interface QueryParams {
    pageNo: number;
    pageSize: number;
    keyword?: string;
    startDate?: string;
    endDate?: string;
  }

  /** 报表项 */
  export interface ReportItem {
    id: string;
    name: string;
    amount: number;
    createTime: string;
  }

  /** 列表响应 */
  export interface ListResult {
    list: ReportItem[];
    total: number;
  }
}

// 定义路径
enum API {
  LIST = '/report-api/payment/list',
  DETAIL = '/report-api/payment/detail',
  EXPORT = '/report-api/payment/export',
}

/**
 * 获取报表列表
 */
export async function getReportListApi(params: ReportApi.QueryParams) {
  return requestClient.get<ReportApi.ListResult>(API.LIST, { params });
}

/**
 * 获取报表详情
 */
export async function getReportDetailApi(id: string) {
  return requestClient.get<ReportApi.ReportItem>(`${API.DETAIL}/${id}`);
}

/**
 * 导出报表
 */
export async function exportReportApi(params: ReportApi.QueryParams) {
  return requestClient.post(API.EXPORT, params, {
    responseType: 'blob',
  });
}
```

## 请求拦截器配置

请求客户端已配置以下拦截器（在 `request.ts` 中）：

1. **请求拦截器**
   - 自动添加 `Authorization` 请求头
   - 自动添加 `Accept-Language` 请求头

2. **响应拦截器**
   - 自动解析响应数据（返回 `data` 字段）
   - Token 过期自动刷新或跳转登录
   - 统一错误提示（使用 `message.error`）

3. **认证处理**
   - 支持 SSO 统一认证
   - 支持本地登录模式
   - Token 失效自动跳转认证中心

## 注意事项

1. **必须使用 `requestClient`**：不要直接使用 `axios`
2. **必须定义类型**：所有请求和响应都要有 TypeScript 类型
3. **必须使用 enum API**：所有路径都要定义为枚举常量
4. **导入别名**：使用 `#/api/request` 而非相对路径
5. **错误处理**：特殊错误码在业务代码中处理，不要修改拦截器
6. **超时时间**：默认 30 秒，特殊需求可在请求时覆盖
7. **文件上传**：使用 `uploadFile` 函数，不要直接调用 `requestClient.upload`
