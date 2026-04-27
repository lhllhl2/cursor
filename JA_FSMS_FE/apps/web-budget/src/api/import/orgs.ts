import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

enum UpdateKey {
  BZ_LEVEL = 'bzLevel',
  CONTROL_LEVEL = 'controlLevel',
  MANAGE_ORG = 'orgCd',
  ERP_DEPART = 'erpDepart',
}

enum API {
  MANAGE_ORG = '/api/ehr/budgetOrg',
  ORGS_EXPORT = '/api/ehr/export',
  ORGS_IMPORT = '/api/ehr/import',
  ORGS_PAGE = '/api/ehr/searchPage',
  ORGS_SYNC = '/api/ehr/sync',
  ORGS_SYNC_ONE_R = '/budget-api/sync/ehr-org-m/synEhrManageOneRData',
  ORGS_SYNC_R = '/budget-api/sync/ehr-org-m/syncEhrManageRData',
  ORGS_UPDATE_bzLevel = '/api/ehr/changeBzLevel',
  ORGS_UPDATE_controlLevel = '/api/ehr/changeControlLevel',
  ORGS_UPDATE_orgCd = '/api/ehr/changeBudget',
  ORGS_UPDATE_erpDepart = '/api/ehr/changeErpDepart',
}

async function getOrgsPage(params: Recordable<any>) {
  return requestClient.post(API.ORGS_PAGE, params);
}

async function exportOrgs(params: Recordable<any>) {
  return requestClient.post(API.ORGS_EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

async function importOrgs(params: UploadFileParams) {
  return requestClient.upload(API.ORGS_IMPORT, {
    ...params,
  });
}

function updateOrgs({
  key,
  params,
}: {
  key: UpdateKey;
  params: Recordable<any>;
}) {
  return requestClient.post(API[`ORGS_UPDATE_${key}`], params);
}

function getManageOrg() {
  return requestClient.post(API.MANAGE_ORG);
}

function syncOrgs() {
  return requestClient.get(API.ORGS_SYNC);
}

/**
 * 同步 EHR 管理一级 R 数据（接口可能较慢，单独延长超时）
 */
function syncEhrManageOneRApiData() {
  return requestClient.post(API.ORGS_SYNC_ONE_R, undefined, {
    timeout: 600_000, // 10 分钟
  });
}

/**
 * 同步 EHR 管理 R 数据（接口可能较慢，单独延长超时）
 */
function syncEhrManageRApiData() {
  return requestClient.post(API.ORGS_SYNC_R, undefined, {
    timeout: 600_000, // 10 分钟
  });
}
export {
  exportOrgs,
  getManageOrg,
  getOrgsPage,
  importOrgs,
  syncEhrManageOneRApiData,
  syncEhrManageRApiData,
  syncOrgs,
  UpdateKey,
  updateOrgs,
};
