import type { Recordable } from '@vben/types';

import type { UploadFileParams } from '#/api/upload';

import { requestClient } from '#/api/request';

export namespace SystemUserApi {
  export interface SystemUser {
    [key: string]: any;
  }
}

enum API {
  ADD_USER_GROUP_TREE = '/admin-api/system/userGroup/addUserGroupTree',
  COPY_USER_GROUP_TREE = '/admin-api/system/userGroup/copyUserGroupTree',
  SAVE_USER_GROUP_BY_TREE = '/admin-api/system/userGroup/saveUserGroupByTree',
  USER_EXPORT = '/admin-api/system/user/batchExport',
  USER_GROUP_TREE = '/admin-api/system/userGroup/getUserGroupTree',
  USER_IMPORT = '/admin-api/system/user/readUserInfoFromExcel',
  USER_LIST = '/admin-api/system/user/userPage',
  USER_RESET_PWD = '/admin-api/system/user/resetPwd',
}

async function getUserList(params: Recordable<any>) {
  return requestClient.post(API.USER_LIST, params);
}

async function exportUserList(params: Recordable<any>) {
  return requestClient.get(API.USER_EXPORT, {
    responseType: 'blob',
    responseReturn: 'raw',
    params,
  });
}

function importUserList(params: UploadFileParams) {
  return requestClient.upload(API.USER_IMPORT, {
    ...params,
  });
}

/**
 * 获取用户组树（根据用户ID）
 * @param id 用户ID
 */
async function getUserGroupTree(id: string) {
  return requestClient.get(API.USER_GROUP_TREE, {
    params: { id, _t: Date.now() },
    headers: {
      'Cache-Control': 'no-cache',
      Pragma: 'no-cache',
    },
  });
}

/**
 * 保存用户组（根据树结构）
 * @param params 保存参数 { userId: string, tree: Array }
 */
async function saveUserGroupByTree(params: Recordable<any>) {
  return requestClient.post(API.SAVE_USER_GROUP_BY_TREE, params);
}

/**
 * 复制用户组树
 * @param params 复制参数 { fromUserId: string, toUserIds: string[] }
 */
async function copyUserGroupTree(params: Recordable<any>) {
  return requestClient.post(API.COPY_USER_GROUP_TREE, params);
}

/**
 * 新增用户组树
 * @param params 新增参数 { fromUserId: string, toUserIds: string[] }
 */
async function addUserGroupTree(params: Recordable<any>) {
  return requestClient.post(API.ADD_USER_GROUP_TREE, params);
}

/**
 * 重置用户密码
 * @param params 重置密码参数
 * @param params.userIds 用户ID数组
 */
async function resetUserPassword(params: { userIds: string[] }) {
  return requestClient.post(API.USER_RESET_PWD, params);
}

export {
  addUserGroupTree,
  copyUserGroupTree,
  exportUserList,
  getUserGroupTree,
  getUserList,
  importUserList,
  resetUserPassword,
  saveUserGroupByTree,
};
