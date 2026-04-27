import { requestClient } from '#/api/request';

enum API {
  LANGS_ALL = '/admin-api/system/i18n-menu/getLangs',
  LANGS_GET = '/admin-api/system/i18n-menu/get',
  LANGS_UPDATE = '/admin-api/system/i18n-menu/save',
}

interface Langs {
  [key: string]: string | string[];
}

/**
 * 获取当前语言
 */
async function getLangsAll() {
  return requestClient.get(API.LANGS_ALL);
}

async function getLangs(params: Langs) {
  return requestClient.get(API.LANGS_GET, { params }).then((res) => {
    const newData: any[] = [];
    if (res) {
      Object.entries(res).forEach(
        ([key, value]: [string, any], index: number) => {
          newData.push({
            id: index,
            title: key,
            ...value,
          });
        },
      );
    }
    return newData;
  });
}

/**
 * 更新语言
 */
async function updateLangs(params: any) {
  return requestClient.post(API.LANGS_UPDATE, params);
}

export { getLangs, getLangsAll, updateLangs };
