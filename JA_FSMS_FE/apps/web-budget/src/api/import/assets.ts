import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

enum API {
  ASSETS_PAGE = '/budget-api/budget-asset-type-mapping/page',
  ASSETS_EXPORT = '/budget-api/budget-asset-type-mapping/export',
  ASSETS_IMPORT = '/budget-api/budget-asset-type-mapping/import',
  ASSETS_UPDATE = '/budget-api/budget-asset-type-mapping/update',
  ASSETS_SYNC = '/budget-api/budget-asset-type-mapping/sync',
}

async function getAssetsPage(params: Recordable<any>) {
  return requestClient.post(API.ASSETS_PAGE, params);
}

async function exportAssets(params: Recordable<any>) {
  return requestClient.post(API.ASSETS_EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

async function importAssets(params: UploadFileParams) {
  return requestClient.upload(API.ASSETS_IMPORT, {
    ...params,
  });
}

async function updateAssets(params: Recordable<any>) {
  return requestClient.post(API.ASSETS_UPDATE, params);
}

function syncAssets() {
  return requestClient.post(API.ASSETS_SYNC, {});
}

export {
  exportAssets,
  getAssetsPage,
  importAssets,
  syncAssets,
  updateAssets,
};
