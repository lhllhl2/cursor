import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemRoleApi {
  export interface SystemRole {
    [key: string]: any;
    id?: string;
    name?: string;
    menuIds?: string[];
    remark?: string;
    status?: string;
  }
}

enum API {
  ROLE_CREATE = '/admin-api/system/role/saveRole',
  ROLE_DELETE = '/admin-api/system/role/del',
  ROLE_LIST = '/admin-api/system/role/rolePage',
  ROLE_MENU_TREE = '/admin-api/system/role/getMenuTreeByRoleId',
  ROLE_UPDATE = '/admin-api/system/role/editRole',
  ROLE_USERGROUP = '/admin-api/system/role/searchUserGroupByRole',
}

async function getRoleList(params: Recordable<any>) {
  return requestClient.post<Array<SystemRoleApi.SystemRole>>(
    API.ROLE_LIST,
    params,
  );
}

async function createRole(data: SystemRoleApi.SystemRole) {
  return requestClient.post(API.ROLE_CREATE, data);
}

async function updateRole(data: SystemRoleApi.SystemRole) {
  return requestClient.post(API.ROLE_UPDATE, data);
}

async function deleteRole(id?: string) {
  return requestClient.post(API.ROLE_DELETE, { id });
}

async function getRoleMenuTree(roleId: string) {
  return requestClient.post(API.ROLE_MENU_TREE, { roleId });
}

async function getRoleUserGroup(id: string) {
  return requestClient.post(API.ROLE_USERGROUP, { id });
}

export {
  createRole,
  deleteRole,
  getRoleList,
  getRoleMenuTree,
  getRoleUserGroup,
  updateRole,
};
