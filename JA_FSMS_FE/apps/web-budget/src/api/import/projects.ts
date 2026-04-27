import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

enum API {
  PROJECTS_API_SYNC = '/budget-api/sync/ehr-org-m/syncProjectControlRData',
  PROJECTS_EXPORT = '/api/proj/export',
  PROJECTS_IMPORT = '/api/proj/import',
  PROJECTS_PAGE = '/api/proj/searchPage',
  PROJECTS_SYNC = '/api/proj/sync',
  PROJECTS_UPDATE_controlLevel = '/api/proj/changeControlLevel',
}

async function getProjectsPage(params: Recordable<any>) {
  return requestClient.post(API.PROJECTS_PAGE, params);
}

async function exportProjects(params: Recordable<any>) {
  return requestClient.post(API.PROJECTS_EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

async function importProjects(params: UploadFileParams) {
  return requestClient.upload(API.PROJECTS_IMPORT, {
    ...params,
  });
}

async function syncProjects() {
  return requestClient.get(API.PROJECTS_SYNC);
}

async function updateProjects(params: Recordable<any>) {
  return requestClient.post(API.PROJECTS_UPDATE_controlLevel, params);
}

/**
 * 接口数据更新
 */
function syncProjectApiData() {
  return requestClient.post(API.PROJECTS_API_SYNC);
}

export {
  exportProjects,
  getProjectsPage,
  importProjects,
  syncProjectApiData,
  syncProjects,
  updateProjects,
};
