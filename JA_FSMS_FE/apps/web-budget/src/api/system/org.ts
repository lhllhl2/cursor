import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

export namespace SystemOrgApi {
  export interface SystemOrg {
    id?: string;
    name: string;
    code: string;
    type: string;
    status: string;
    userGroupIds: string[];
    children?: SystemOrgApi.SystemOrg[];
    themes: string[];
  }
}

enum API {
  ORG_BATCH_OPERATE = '/admin-api/system/org/bind-user-groups',
  ORG_EXPORT = '/admin-api/system/org/batch-export',
  ORG_IMPORT = '/admin-api/system/org/syncOrgUserGroupRelation',
  ORG_LEGAL_LIST = '/admin-api/system/legal-org/list', // 法人组织
  ORG_LIST = '/admin-api/system/org/list',
  ORG_SYNC = '/admin-api/system/org/sync-original-organization-to-business',
  ORG_USERGROUP = '/admin-api/system/userGroup/getGroupIdsByOrgId',
}

async function getOrgList(params: Recordable<any>) {
  return requestClient.post<Array<SystemOrgApi.SystemOrg>>(
    API.ORG_LIST,
    params,
  );
}

async function orgBatchOperate(params: Recordable<any>) {
  return requestClient.post(API.ORG_BATCH_OPERATE, params);
}

async function getOrgUserGroup(params: Recordable<any>) {
  return requestClient.get(API.ORG_USERGROUP, { params });
}
// 同步组织
async function orgSync(params: Recordable<any>) {
  return requestClient.post(API.ORG_SYNC, params);
}

async function exportOrgList(params: Recordable<any>) {
  return requestClient.post(API.ORG_EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

function importOrgList(params: UploadFileParams) {
  return requestClient.upload(API.ORG_IMPORT, {
    ...params,
  });
}

export {
  exportOrgList,
  getOrgList,
  getOrgUserGroup,
  importOrgList,
  orgBatchOperate,
  orgSync,
};
