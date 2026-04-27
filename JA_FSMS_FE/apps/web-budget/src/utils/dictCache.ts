import { $te } from '@vben/locales';

import { $t } from '#/locales';
/**
 * 字典缓存工具类
 * 提供内存缓存功能，避免重复请求字典数据
 */

interface CacheItem<T = any> {
  data: T;
  timestamp: number;
  expireTime: number;
}

class DictCache {
  private cache = new Map<string, CacheItem>();
  private defaultExpireTime = 60 * 60 * 1000; // 默认60分钟过期

  /**
   * 清理过期缓存
   */
  cleanExpired(): void {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expireTime) {
        this.cache.delete(key);
      }
    }
  }

  /**
   * 清空所有缓存
   */
  clear(): void {
    this.cache.clear();
  }

  /**
   * 删除指定缓存
   * @param key 缓存键
   */
  delete(key: string): void {
    this.cache.delete(key);
  }

  /**
   * 获取缓存
   * @param key 缓存键
   * @returns 缓存数据或null
   */
  get<T>(key: string): null | T {
    const item = this.cache.get(key);

    if (!item) {
      return null;
    }

    // 检查是否过期
    if (Date.now() > item.expireTime) {
      this.cache.delete(key);
      return null;
    }

    return item.data as T;
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    const now = Date.now();
    let validCount = 0;
    let expiredCount = 0;

    for (const item of this.cache.values()) {
      if (now > item.expireTime) {
        expiredCount++;
      } else {
        validCount++;
      }
    }

    return {
      total: this.cache.size,
      valid: validCount,
      expired: expiredCount,
    };
  }

  /**
   * 检查缓存是否存在且未过期
   * @param key 缓存键
   * @returns 是否存在有效缓存
   */
  has(key: string): boolean {
    const item = this.cache.get(key);

    if (!item) {
      return false;
    }

    // 检查是否过期
    if (Date.now() > item.expireTime) {
      this.cache.delete(key);
      return false;
    }

    return true;
  }

  /**
   * 设置缓存
   * @param key 缓存键
   * @param data 缓存数据
   * @param expireTime 过期时间（毫秒），默认5分钟
   */
  set<T>(key: string, data: T, expireTime?: number): void {
    const now = Date.now();
    const expire = expireTime || this.defaultExpireTime;

    this.cache.set(key, {
      data,
      timestamp: now,
      expireTime: now + expire,
    });
  }

  /**
   * 设置默认过期时间
   * @param time 过期时间（毫秒）
   */
  setDefaultExpireTime(time: number): void {
    this.defaultExpireTime = time;
  }
}

// 导出类型
export type { CacheItem };

// 创建全局字典缓存实例
export const dictCache = new DictCache();

// 翻译字典项
export function transformDictItem(list: any) {
  return list.map((item: any) => ({
    label:
      item.fieldLabel && $te(item.fieldLabel)
        ? $t(item.fieldLabel)
        : item.fieldLabel,
    value: item.fieldKey,
  }));
}
