/**
 * 菜单节点接口定义
 */
interface MenuNode {
  id: number | string;
  name: string;
  title: string;
  pid: number | string;
  jsonData: Record<string, any>;
  children?: MenuNode[] | null;
}

/**
 * 语言包结构接口定义
 */
interface LocaleData {
  [key: string]: any;
}

/**
 * 转换后的语言包结构
 */
interface TransformedLocaleData {
  'zh-CN': LocaleData;
  'en-US': LocaleData;
}

// 清理零宽字符的函数
function cleanZeroWidthChars(str: string): string {
  return str.replaceAll(/[\u200B-\u200D\uFEFF]/g, '');
}

/**
 * 将后端返回的嵌套菜单结构转换为层级化的语言包结构
 * @param data 后端返回的菜单数据
 * @returns 转换后的语言包结构
 */
export function transformMenuDataToLocale(
  data: MenuNode | MenuNode[],
): TransformedLocaleData {
  const result: TransformedLocaleData = {
    'zh-CN': {},
    'en-US': {},
  };

  /**
   * 递归处理菜单数据
   * @param node 当前节点
   * @param parentObj 父级对象
   */
  function processNode(
    node: MenuNode,
    parentObj: Record<string, any> = result['zh-CN'],
    parentObjEn: Record<string, any> = result['en-US'],
  ) {
    if (!node || !node.jsonData) return;

    const { jsonData } = node;

    // 清理节点名称中的零宽字符
    const cleanName = cleanZeroWidthChars(node.name);

    // 确保当前节点在父级对象中存在
    if (!parentObj[cleanName]) parentObj[cleanName] = {};
    if (!parentObjEn[cleanName]) parentObjEn[cleanName] = {};

    const currentObj = parentObj[cleanName] as Record<string, any>;
    const currentObjEn = parentObjEn[cleanName] as Record<string, any>;

    // 处理当前节点的翻译数据
    Object.entries(jsonData).forEach(([key, value]) => {
      if (typeof value === 'object' && value !== null) {
        // 如果值是对象，说明包含多语言数据
        Object.entries(value).forEach(([lang, text]) => {
          if (lang === 'zh-CN') {
            currentObj[key] = text;
          } else if (lang === 'en-US') {
            currentObjEn[key] = text;
          }
        });
      }
    });

    // 递归处理子节点
    if (node.children && Array.isArray(node.children)) {
      node.children.forEach((child) => {
        processNode(child, currentObj, currentObjEn);
      });
    }
  }

  // 处理根节点
  if (Array.isArray(data)) {
    data.forEach((item) => processNode(item));
  } else {
    processNode(data);
  }

  return result;
}
