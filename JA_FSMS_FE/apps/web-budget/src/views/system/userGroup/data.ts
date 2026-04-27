import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserGroupApi } from '#/api';

import { fetchDictByCode } from '#/api/system/dict';
import { DIC_ENUM } from '#/enums';
import { $t } from '#/locales';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'userGroupName',
      label: $t('system.userGroup.displayName'),
    },
    {
      component: 'ApiSelect',
      fieldName: 'types',
      label: $t('system.userGroup.type'),
      componentProps: {
        allowClear: true,
        api: fetchDictByCode,
        class: 'w-full',
        mode: 'multiple',
        params: {
          code: DIC_ENUM.USER_GROUP_TYPE,
        },
      },
    },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemUserGroupApi.SystemUserGroup>,
): VxeTableGridOptions<SystemUserGroupApi.SystemUserGroup>['columns'] {
  return [
    {
      field: 'name',
      title: $t('system.userGroup.displayName'),
      align: 'left',
      minWidth: 250,
    },
    // {
    //   field: 'code',
    //   title: $t('system.userGroup.code'),
    // },
    {
      field: 'type',
      width: 100,
      title: $t('system.userGroup.type'),
      cellRender: {
        name: 'DictCell',
      },
    },
    {
      field: 'userCount',
      title: $t('system.userGroup.userCount'),
      width: 100,
    },
    {
      field: 'remark',
      title: $t('system.userGroup.remark'),
      align: 'left',
      minWidth: 400,
    },
    {
      field: 'creator',
      title: $t('system.userGroup.creator'),
      width: 100,
    },
    {
      field: 'createTime',
      title: $t('system.userGroup.createTime'),
      width: 150,
    },
    {
      field: 'updater',
      title: $t('system.userGroup.updater'),
      width: 100,
    },
    {
      field: 'updateTime',
      title: $t('system.userGroup.updateTime'),
      width: 150,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'name',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'edit',
            text: $t('system.userGroup.userMember'),
          },
          {
            code: 'editInfo',
            text: $t('common.edit'),
          },
          'delete', // 默认的删除按钮
        ],
      },
      field: 'operation',
      fixed: 'right',

      showOverflow: false,
      title: $t('common.operation'),
      width: 200,
    },
  ];
}

export function useFirstFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.userGroup.displayName'),
      rules: 'required',
    },
    {
      component: 'ApiSelect',
      fieldName: 'type',
      label: $t('system.userGroup.type'),
      componentProps: {
        allowClear: true,
        api: fetchDictByCode,
        params: {
          code: DIC_ENUM.USER_GROUP_TYPE,
        },
      },
      rules: 'required',
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.userGroup.remark'),
    },
  ];
}

export function useEditFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.userGroup.displayName'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.userGroup.remark'),
    },
  ];
}

export const userGroupTypeColor: Record<string, string> = {
  '1': 'processing', // 蓝色 - 菜单
  '2': 'success', // 绿色 - 报表
  '3': 'error', // 红色 - 组织
};

export const stepItems = [
  {
    title: $t('system.userGroup.basicInfo'),
  },
  // {
  //   title: $t('system.userGroup.org'),
  // },
  // {
  //   title: $t('system.userGroup.form'),
  // },
  {
    title: $t('system.userGroup.user'),
  },
];
