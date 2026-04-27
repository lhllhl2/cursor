/**
 * 空函数，不执行任何操作
 * 通常用作默认的回调函数或占位符
 */
export const noop = (): void => {
  // 空实现，不执行任何操作
};

/**
 * 返回 Promise 的空函数
 * 用于需要返回 Promise 的默认回调函数
 */
export const noopAsync = <T = any>(): Promise<T> => {
  return Promise.resolve() as Promise<T>;
};

/**
 * 防抖函数
 * @param func 要防抖的函数
 * @param delay 延迟时间（毫秒）
 * @param immediate 是否立即执行
 * @returns 防抖后的函数
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  delay: number,
  immediate: boolean = false,
): ((...args: Parameters<T>) => Promise<ReturnType<T>>) => {
  let timeoutId: NodeJS.Timeout | null = null;

  return (...args: Parameters<T>): Promise<ReturnType<T>> => {
    return new Promise((resolve) => {
      const later = () => {
        timeoutId = null;
        if (!immediate) {
          const result = func(...args);
          resolve(result);
        }
      };

      const callNow = immediate && !timeoutId;

      if (timeoutId) {
        clearTimeout(timeoutId);
      }

      timeoutId = setTimeout(later, delay);

      if (callNow) {
        const result = func(...args);
        resolve(result);
      }
    });
  };
};

/**
 * 节流函数
 * @param func 要节流的函数
 * @param delay 延迟时间（毫秒）
 * @returns 节流后的函数
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  delay: number,
  options: { leading?: boolean; trailing?: boolean } = {},
): ((...args: Parameters<T>) => Promise<ReturnType<T>>) => {
  const { leading = true, trailing = true } = options;
  let timeoutId: NodeJS.Timeout | null = null;
  let lastExecTime = 0;

  return (...args: Parameters<T>): Promise<ReturnType<T>> => {
    return new Promise((resolve) => {
      const now = Date.now();

      if (!lastExecTime && !leading) {
        lastExecTime = now;
      }

      const remaining = delay - (now - lastExecTime);

      if (remaining <= 0 || remaining > delay) {
        if (timeoutId) {
          clearTimeout(timeoutId);
          timeoutId = null;
        }
        lastExecTime = now;
        const result = func(...args);
        resolve(result);
      } else if (!timeoutId && trailing) {
        timeoutId = setTimeout(() => {
          lastExecTime = Date.now();
          timeoutId = null;
          const result = func(...args);
          resolve(result);
        }, remaining);
      }
    });
  };
};

/**
 * 深度拷贝函数
 * 支持基本类型、对象、数组、Date、RegExp、Map、Set 等
 * @param obj 要拷贝的对象
 * @param hash 用于处理循环引用的 WeakMap
 * @returns 深度拷贝后的对象
 */
export const deepClone = <T>(obj: T, hash = new WeakMap()): T => {
  // 处理 null 和基本类型
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  // 处理 Date 对象
  if (obj instanceof Date) {
    return new Date(obj) as T;
  }

  // 处理 RegExp 对象
  if (obj instanceof RegExp) {
    return new RegExp(obj.source, obj.flags) as T;
  }

  // 处理循环引用
  if (hash.has(obj)) {
    return hash.get(obj);
  }

  // 处理 Map
  if (obj instanceof Map) {
    const newMap = new Map();
    hash.set(obj, newMap);
    obj.forEach((value, key) => {
      newMap.set(deepClone(key, hash), deepClone(value, hash));
    });
    return newMap as T;
  }

  // 处理 Set
  if (obj instanceof Set) {
    const newSet = new Set();
    hash.set(obj, newSet);
    obj.forEach((value) => {
      newSet.add(deepClone(value, hash));
    });
    return newSet as T;
  }

  // 处理数组
  if (Array.isArray(obj)) {
    const newArray: any[] = [];
    hash.set(obj, newArray);
    for (const [i, element] of obj.entries()) {
      newArray[i] = deepClone(element, hash);
    }
    return newArray as T;
  }

  // 处理普通对象
  const newObj = {} as any;
  hash.set(obj, newObj);

  // 获取所有属性（包括不可枚举的）
  const allProps = Object.getOwnPropertyNames(obj);
  const symbolProps = Object.getOwnPropertySymbols(obj);

  // 拷贝普通属性
  allProps.forEach((key) => {
    newObj[key] = deepClone((obj as any)[key], hash);
  });

  // 拷贝 Symbol 属性
  symbolProps.forEach((symbol) => {
    newObj[symbol] = deepClone((obj as any)[symbol], hash);
  });

  return newObj;
};

/**
 * Converts URL query parameters to an object
 * @param {string} url - The URL containing query parameters
 * @returns {Record<string, string>} Object containing the parsed query parameters
 */
export function param2Obj(url: string): Record<string, string> {
  const search = decodeURIComponent(url.split('?')[1] || '').replaceAll(
    '+',
    ' ',
  );
  if (!search) {
    return {};
  }
  const obj: Record<string, string> = {};
  const searchArr = search.split('&');
  searchArr.forEach((v) => {
    const index = v.indexOf('=');
    if (index !== -1) {
      const name = v.slice(0, Math.max(0, index));
      const val = v.slice(index + 1);
      obj[name] = val;
    }
  });
  return obj;
}

/**
 * 解析字符串中的转义符
 * @param msg 包含转义符的字符串
 * @returns 解析后的字符串
 */
export function parseString(msg: string): string {
  if (!msg) return '';
  return msg
    .replaceAll(String.raw`\n`, '\n')
    .replaceAll(String.raw`\t`, '\t')
    .replaceAll(String.raw`\r`, '\r')
    .replaceAll('\\\\', '\\');
}

// 判断金额是否为空
export function isNullOrUndefined(amount: number | string): boolean {
  return amount === null || amount === undefined || amount === '';
}

// 金额格式化,千分位(,)分隔，保留两位小数
export function formatMoney(amount: number | string): string {
  if (isNullOrUndefined(amount)) return '-';
  if (amount === 0) return '0.00';
  return Number(amount)
    .toFixed(2)
    .replaceAll(/\B(?=(\d{3})+(?!\d))/g, ',');
}
interface FlattenArrayOptions {
  array: any[];
  pid?: number;
  childrenField?: string;
}

/**
 * 扁平化树形数组，返回每个节点附带其父节点 id 的一维数组
 * @param options 配置项
 * @param options.array 要扁平化的数组
 * @param options.pid 父节点的 id
 * @param options.childrenField 子节点字段名，默认 'children'
 * @returns 扁平化后的数组，每项为去除子节点字段的新对象
 */
export function flattenArray(options: FlattenArrayOptions): any[] {
  const { array = [], pid, childrenField = 'children' } = options;
  const result: any[] = [];

  for (const item of array) {
    const children = item?.[childrenField];

    if (Array.isArray(children) && children.length > 0) {
      result.push(
        ...flattenArray({
          array: children,
          pid: item.id,
          childrenField,
        }),
      );
    }

    // 通过解构省略 childrenField，避免 delete 动态属性
    const { [childrenField]: _omit, ...rest } = item;
    result.push({ ...rest, pid });
  }

  return result;
}

/**
 * 判断对象是否为空
 * @param obj 要判断的对象
 * @returns 是否为空
 */
export function isEmptyObject(obj: Record<string, any>): boolean {
  if (!obj) return true;
  return Object.keys(obj).length === 0;
}
