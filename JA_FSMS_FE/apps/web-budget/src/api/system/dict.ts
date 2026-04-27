import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';
import { dictCache, transformDictItem } from '#/utils';

export namespace SystemDictApi {
  export interface DictOption {
    fieldKey: string;
    fieldLabel: string;
  }

  export interface SystemDict {
    [key: string]: any;
    id?: string;
    code?: string;
    title: string;
    options?: DictOption[];
    labelList?: DictOption[];
  }
}

enum API {
  DICT_CREATE = '/admin-api/system/dict/add',
  DICT_DELETE = '/admin-api/system/dict/del',
  DICT_LIST = '/admin-api/system/dict/page',
  DICT_UPDATE = '/admin-api/system/dict/edit',
  Fetch_DICT = '/admin-api/system/dict/getByCode',
}

async function getDictList(params: Recordable<any>) {
  return requestClient.post<Array<SystemDictApi.SystemDict>>(
    API.DICT_LIST,
    params,
  );
}

async function createDict(data: SystemDictApi.SystemDict) {
  return requestClient.post(API.DICT_CREATE, data);
}

async function updateDict(data: SystemDictApi.SystemDict) {
  return requestClient.post(API.DICT_UPDATE, data);
}

async function deleteDict(id?: string) {
  return requestClient.post(API.DICT_DELETE, { id });
}

async function fetchDictByCode(params: any) {
  const { code } = params;
  const cacheKey = `dict_${code}`;

  // 尝试从缓存获取
  const cachedData = dictCache.get(cacheKey);
  if (cachedData) {
    return cachedData;
  }

  // 缓存未命中，发起请求
  const result = await requestClient
    .post(API.Fetch_DICT, { codes: [code] })
    .then((res) => transformDictItem(res[code].labelList));

  // 将结果存入缓存
  dictCache.set(cacheKey, result);

  return result;
}

/**
 * 清除指定字典的缓存
 * @param code 字典代码
 */
function clearDictCache(code: string) {
  const cacheKey = `dict_${code}`;
  dictCache.delete(cacheKey);
}

/**
 * 清除所有字典缓存
 */
function clearAllDictCache() {
  dictCache.clear();
}

/**
 * 刷新指定字典缓存（清除缓存并重新获取）
 * @param code 字典代码
 */
async function refreshDictCache(code: string) {
  clearDictCache(code);
  return fetchDictByCode({ code });
}

/**
 * 获取字典缓存统计信息
 */
function getDictCacheStats() {
  return dictCache.getStats();
}

export {
  clearAllDictCache,
  clearDictCache,
  createDict,
  deleteDict,
  fetchDictByCode,
  getDictCacheStats,
  getDictList,
  refreshDictCache,
  updateDict,
};
