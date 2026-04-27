import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

// import { getUserGroupListByType } from '#/api';
import { PERMISSION_ENUM } from '#/enums';
import { $t } from '#/locales';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'displayName',
      label: $t('system.user.displayName'),
      componentProps: {
        allowClear: true,
      },
    },
    {
      component: 'Input',
      fieldName: 'userName',
      label: $t('system.user.code'),
      componentProps: {
        allowClear: true,
      },
    },
    {
      component: 'Input',
      fieldName: 'userGroupIds',
      label: $t('system.role.userGroup'),
      componentProps: {
        allowClear: true,
      },
    },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemUserApi.SystemUser>,
): VxeTableGridOptions<SystemUserApi.SystemUser>['columns'] {
  return [
    {
      field: 'displayName',
      title: $t('system.user.displayName'),
      minWidth: 100,
    },
    {
      field: 'email',
      title: $t('system.user.email'),
      width: 200,
    },
    {
      field: 'userName',
      width: 100,
      title: $t('system.user.code'),
    },
    {
      field: 'phoneNumber',
      title: $t('system.user.phoneNumber'),
      width: 150,
    },
    {
      field: 'gender',
      title: $t('system.user.gender'),
      width: 70,
      cellRender: {
        name: 'DictCell',
      },
    },
    // {
    //   cellRender: {
    //     name: 'userStatus',
    //   },
    //   field: 'leaveStatus',
    //   title: $t('system.user.status'),
    //   width: 100,
    // },
    {
      field: 'officeLocation',
      title: $t('system.user.officeLocation'),
      width: 120,
    },
    {
      field: 'post',
      title: $t('system.user.post'),
      width: 120,
    },
    {
      field: 'birthday',
      title: $t('system.user.birthday'),
      width: 120,
    },
    // {
    //   field: 'inductionDate',
    //   title: $t('system.user.inductionDate'),
    //   width: 120,
    // },
    // {
    //   field: 'leaveDate',
    //   title: $t('system.user.leaveDate'),
    //   width: 120,
    // },
    {
      field: 'directManagerCode',
      title: $t('system.user.directManagerCode'),
      width: 100,
    },
    {
      field: 'groupIds',
      title: $t('system.role.userGroup'),
      minWidth: 220,
      slots: { default: 'userGroup' },
    },
    // {
    //   field: 'createTime',
    //   title: $t('system.user.createTime'),
    //   width: 200,
    // },
    // {
    //   field: 'updateTime',
    //   title: $t('system.user.updateTime'),
    //   width: 200,
    // },
    // {
    //   field: 'creator',
    //   title: $t('system.user.creator'),
    //   width: 200,
    // },
    // {
    //   field: 'updater',
    //   title: $t('system.user.updater'),
    //   width: 200,
    // },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'displayName',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'copy',
            text: $t('common.copy'),
          },
          {
            code: 'resetPassword',
            text: $t('system.user.resetPassword'),
            danger: true,
            access: PERMISSION_ENUM.SystemUserResetPassword,
            title: $t('system.user.resetPassword'),
            content: (row: SystemUserApi.SystemUser) =>
              `确定要重置 ${row.displayName} 的密码吗？`,
          },
        ],
      },
      field: 'operation',
      fixed: 'right',

      showOverflow: false,
      title: $t('common.operation'),
      width: 120,
    },
  ];
}
