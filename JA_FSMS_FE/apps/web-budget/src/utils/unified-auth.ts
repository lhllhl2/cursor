/**
 * 统一认证工具函数
 */

/**
 * 构建统一认证URL
 * @param authUrl 认证中心地址
 * @param additionalParams 额外的参数
 * @returns 完整的认证URL
 */
export function buildUnifiedAuthUrl(
  authUrl: string,
  additionalParams: Record<string, string> = {},
): string {
  const url = new URL(authUrl, window.location.origin);
  Object.entries(additionalParams).forEach(([key, value]) => {
    url.searchParams.set(key, value);
  });

  return url.toString();
}

/**
 * 解析统一认证回调参数
 * @returns 解析后的参数对象
 */
export function parseUnifiedAuthCallback(): {
  accessToken?: string;
  error?: string;
  errorDescription?: string;
  redirect?: string;
  token?: string;
} {
  const urlParams = new URLSearchParams(window.location.search);

  return {
    token: urlParams.get('token') || undefined,
    accessToken: urlParams.get('accessToken') || undefined,
    redirect: urlParams.get('redirect') || undefined,
    error: urlParams.get('error') || undefined,
    errorDescription: urlParams.get('error_description') || undefined,
  };
}

/**
 * 检查是否为统一认证回调
 * @returns 是否为认证回调
 */
export function isUnifiedAuthCallback(): boolean {
  const params = parseUnifiedAuthCallback();
  return !!(params.token || params.accessToken || params.error);
}

/**
 * 清除URL中的认证参数
 */
export function clearAuthParams(): void {
  const url = new URL(window.location.href);
  const paramsToRemove = [
    'token',
    'accessToken',
    'redirect',
    'error',
    'error_description',
  ];

  paramsToRemove.forEach((param) => {
    url.searchParams.delete(param);
  });

  window.history.replaceState({}, '', url.toString());
}

/**
 * 获取当前页面完整路径（包含查询参数）
 * 注意：此函数需要在 Vue 组件或 composable 中使用，或者传入 router 实例
 * @param route - Vue Router 的 route 对象（可选）
 * @returns 当前页面路径
 */
export function getCurrentFullPath(route?: any): string {
  // 如果传入了 route 对象，优先使用 route.fullPath
  // route.fullPath 会自动处理 hash 和 history 模式
  debugger;
  if (route?.fullPath) {
    return route.fullPath;
  }

  // 检查是否为 hash 模式
  const isHashMode = import.meta.env.VITE_ROUTER_HISTORY === 'hash';

  if (isHashMode) {
    // hash 模式下，从 hash 中提取路径和 queryString
    const hash = window.location.hash;
    if (hash) {
      // 移除开头的 #，返回 /path?query=value
      return hash.slice(1);
    }
    return window.location.pathname;
  } else {
    // history 模式下，使用 pathname + search
    return window.location.pathname + window.location.search;
  }
}
