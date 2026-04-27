import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

export namespace SystemProjectApi {
  export interface SystemProject {
    id?: string;
    name: string;
    code: string;
    type: string;
    status: string;
    userGroupIds: string[];
    children?: SystemProjectApi.SystemProject[];
    themes: string[];
  }
}

enum API {
  PROJECT_BIND_USERGROUP = '/admin-api/system/project/bindUserGroup',
  PROJECT_EXPORT = '/admin-api/system/project/export',
  PROJECT_IMPORT = '/admin-api/system/project/import',
  PROJECT_LIST = '/admin-api/system/project/list',
  PROJECT_SYNC = '/admin-api/system/project/sync',
  PROJECT_USERGROUP = '/admin-api/system/project/getGroupIdsByProjectId',
}

async function getProjectList(params: Recordable<any>) {
  return requestClient.post<Array<SystemProjectApi.SystemProject>>(
    API.PROJECT_LIST,
    params,
  );
}

async function getProjectUserGroup(params: Recordable<any>) {
  return requestClient.get(API.PROJECT_USERGROUP, { params });
}

async function bindProjectUserGroup(params: Recordable<any>) {
  return requestClient.post(API.PROJECT_BIND_USERGROUP, params);
}

async function projectSync(params: Recordable<any>) {
  return requestClient.get(API.PROJECT_SYNC, params);
}

async function projectExport(params: Recordable<any>) {
  return requestClient.post(API.PROJECT_EXPORT, params, {
    responseType: 'blob',
    responseReturn: 'raw',
  });
}

async function projectImport(params: UploadFileParams) {
  return requestClient.upload(API.PROJECT_IMPORT, { ...params });
}

export {
  bindProjectUserGroup,
  getProjectList,
  getProjectUserGroup,
  projectExport,
  projectImport,
  projectSync,
};
