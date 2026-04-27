import type { Ref } from 'vue';

/**
 * 1.解决都要try/catch的流程
 * 2.解决发送请求需要手动防止多次重复请求痛点
 * 3.不需要手动Loding了，直接loading
 * @author shawn
 */
import { onMounted, ref, shallowRef } from 'vue';

import { noop, noopAsync } from '../utils/common';

// 基础类型定义
type RequestPromise<T, P = any> = (params: P) => Promise<T>;

interface UseRequestOptions<T, P = any> {
  params?: P;
  initRes?: T;
  execute?: boolean;
  onSuccess?: (res: T, config: any) => void;
  onFailed?: (err: any) => void;
}

interface RequestConfig<T, P = any> {
  request: RequestPromise<T, P>;
  config?: UseRequestOptions<T, P>;
}

// 返回结果类型
interface UseRequestResult<T, P = any> {
  run: (params?: P) => Promise<T>;
  loading: Ref<boolean>;
  res: Ref<T | undefined>;
  reset: () => void;
}

export const useRequest = <T, P = any>(
  requestPromise: RequestPromise<T, P> = noopAsync as RequestPromise<T, P>,
  options: UseRequestOptions<T, P> = {},
): UseRequestResult<T, P> => {
  const {
    params,
    initRes,
    execute = false,
    onSuccess = noop,
    onFailed = noop,
  } = options;
  const res = ref<T | undefined>(initRes);
  const loading = shallowRef(false);

  const run = async (requestParams?: P): Promise<T> => {
    const finalParams = (requestParams ?? params) as P;
    if (loading.value) {
      throw new Error('Request is already in progress');
    }
    loading.value = true;
    try {
      const response = await requestPromise(finalParams);
      res.value = response;
      loading.value = false;
      onSuccess(response, { params: finalParams });
      return response;
    } catch (error) {
      loading.value = false;
      onFailed(error);
      throw error;
    }
  };

  const reset = () => {
    res.value = initRes;
    loading.value = false;
  };

  // 首次执行
  onMounted(() => {
    if (execute) {
      run();
    }
  });

  return { run, loading, res, reset } as UseRequestResult<T, P>;
};

// 类型安全的 useMultiRequest
export const useMultiRequest = <T extends readonly RequestConfig<any, any>[]>(
  configs: T,
) => {
  const results = configs.map(({ request, config }) =>
    useRequest(request, config),
  );

  return results;
};
