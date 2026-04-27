import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

export namespace SystemUserGroupApi {
  export interface SystemUserGroup {
    [key: string]: any;
  }
}

enum API {
  SEARCH_GROUP_USER_LIST = '/admin-api/system/userGroup/searchGroupUserList',
  USER_FOR_GROUP = '/admin-api/system/user/userForGroup',
  USER_GROUP_ADD = '/admin-api/system/userGroup/addUserGroup',
  USER_GROUP_DEL = '/admin-api/system/userGroup/del',
  USER_GROUP_EDIT = '/admin-api/system/userGroup/editUserGroup',
  USER_GROUP_EXPORT = '/admin-api/system/userGroup/batchExport',
  USER_GROUP_IMPORT = '/admin-api/system/userGroup/importUserGroupAndRelations',
  USER_GROUP_PAGE = '/admin-api/system/userGroup/getPage',
  USER_GROUP_USER_SAVE = '/admin-api/system/userGroup/groupUserRSave',
  USERGROUP_LIST_BY_TYPE = '/admin-api/system/userGroup/getList',
}

async function getUserGroupPage(params: Recordable<any>) {
  return requestClient.post(API.USER_GROUP_PAGE, params);
}

async function searchGroupUserList(id: string | undefined) {
  if (!id) return { data: [] };
  return requestClient.post(API.SEARCH_GROUP_USER_LIST, { id });
}

async function addUserGroup(params: Recordable<any>) {
  return requestClient.post(API.USER_GROUP_ADD, params);
}

async function groupUserRSave(params: Recordable<any>) {
  return requestClient.post(API.USER_GROUP_USER_SAVE, params);
}

async function userForGroup(keyword: string) {
  return requestClient.post(API.USER_FOR_GROUP, { keyword });
}

async function deleteUserGroup(id: string) {
  return requestClient.post(API.USER_GROUP_DEL, { id });
}

async function getUserGroupListByType(params: Recordable<any>) {
  return requestClient.post(API.USERGROUP_LIST_BY_TYPE, params);
}

async function exportUserGroupList(params: Recordable<any>) {
  return requestClient.get(API.USER_GROUP_EXPORT, {
    responseType: 'blob',
    responseReturn: 'raw',
    params,
  });
}

function importUserGroupList(params: UploadFileParams) {
  return requestClient.upload(API.USER_GROUP_IMPORT, {
    ...params,
  });
}

async function editUserGroup(params: Recordable<any>) {
  return requestClient.post(API.USER_GROUP_EDIT, params);
}

export {
  addUserGroup,
  deleteUserGroup,
  editUserGroup,
  exportUserGroupList,
  getUserGroupListByType,
  getUserGroupPage,
  groupUserRSave,
  importUserGroupList,
  searchGroupUserList,
  userForGroup,
};
