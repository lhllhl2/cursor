import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemLogApi } from '#/api/system/log';

import { $t } from '#/locales';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'logType',
      label: $t('system.log.logType'), // 类型
      componentProps: {
        placeholder: $t('system.log.logTypePlaceholder'), // 请输入类型
        allowClear: true,
      },
    },
    {
      component: 'Input',
      fieldName: 'userName',
      label: $t('system.log.userName'), // 工号
      componentProps: {
        placeholder: $t('system.log.userNamePlaceholder'), // 请输入工号
        allowClear: true,
      },
    },
  ];
}

// 用户登录日志的列配置
export function useUserLoginColumns(): VxeTableGridOptions<SystemLogApi.UserLoginLog>['columns'] {
  return [
    {
      field: 'userName',
      title: $t('system.log.userName'), // 工号
    },
    {
      field: 'displayName',
      title: $t('system.log.displayName'), // 姓名
    },
    {
      field: 'ip',
      title: $t('system.log.ip'), // IP地址
      width: 300,
    },
    {
      field: 'logType',
      title: $t('system.log.logType'), // 类型
      width: 100,
    },
    {
      field: 'createTime',
      title: $t('system.log.createTime'), // 时间
      width: 300,
    },
  ];
}

// 导出用户登录日志列配置
export function useColumns(): VxeTableGridOptions<SystemLogApi.UserLoginLog>['columns'] {
  return useUserLoginColumns();
}
