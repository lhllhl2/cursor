import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

enum API {
  SUBJECTS_API_SYNC = '/budget-api/sync/ehr-org-m/syncSubjectInfoData',
  SUBJECTS_EXPORT = '/api/subj/export',
  SUBJECTS_IMPORT = '/api/subj/import',
  SUBJECTS_PAGE = '/api/subj/searchPage',
  SUBJECTS_SYNC = '/api/subj/sync',
  SUBJECTS_UPDATE_controlLevel = '/api/subj/changeControlLevel',
}

async function getSubjectsPage(params: Recordable<any>) {
  return requestClient.post(API.SUBJECTS_PAGE, params);
}

async function exportSubjects(params: Recordable<any>) {
  return requestClient.post(API.SUBJECTS_EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

async function importSubjects(params: UploadFileParams) {
  return requestClient.upload(API.SUBJECTS_IMPORT, {
    ...params,
  });
}

async function updateSubjects(params: Recordable<any>) {
  return requestClient.post(API.SUBJECTS_UPDATE_controlLevel, params);
}

function syncSubjects() {
  return requestClient.get(API.SUBJECTS_SYNC);
}

/**
 * 接口数据更新
 */
function syncSubjectApiData() {
  return requestClient.post(API.SUBJECTS_API_SYNC);
}

export {
  exportSubjects,
  getSubjectsPage,
  importSubjects,
  syncSubjectApiData,
  syncSubjects,
  updateSubjects,
};
