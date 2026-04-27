import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemOrgApi } from '#/api';

import { fetchDictByCode, getUserGroupListByType } from '#/api';
import { DIC_ENUM } from '#/enums';
import { $t } from '#/locales';

export function useFormSchema(isBatch: boolean): VbenFormSchema[] {
  const mode = {
    component: 'ApiSelect',
    fieldName: 'importMode',
    label: $t('system.dict.importMode'),
    componentProps: {
      allowClear: true,
      autoSelect: 'first',
      api: fetchDictByCode,
      class: 'w-full',
      params: {
        code: DIC_ENUM.IMPORT_MODE,
      },
    },
    rules: 'required',
  };
  return [
    {
      component: 'Divider',
      fieldName: 'divider1',
      hideLabel: true,
      renderComponentContent() {
        return {
          default: () => $t('system.org.auth'),
        };
      },
    },
    ...(isBatch ? [mode] : []),
    {
      component: 'ApiSelect',
      fieldName: 'userGroupIds',
      label: $t('system.role.userGroup'),
      componentProps: {
        mode: 'multiple',
        showSearch: true,
        optionFilterProp: 'label',
        api: getUserGroupListByType,
        params: {
          types: [2],
        },
        labelField: 'name',
        valueField: 'id',
      },
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'projectName',
      label: $t('system.systemProject.name'),
    },
    {
      component: 'Input',
      fieldName: 'projectCode',
      label: $t('system.systemProject.code'),
    },
  ];
}

export function useColumns<T = SystemOrgApi.SystemOrg>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      fixed: 'left',
      width: 60,
    },
    {
      fixed: 'left',
      align: 'left',
      field: 'projectName',
      title: $t('system.systemProject.name'),
      treeNode: true,
      minWidth: 200,
      cellRender: {
        name: 'CellMerge',
        attrs: {
          merges: ['projectCode', 'projectName'],
        },
      },
    },
    // {
    //   field: 'projectCode',
    //   title: $t('system.systemProject.code'),
    // },
    {
      field: 'updateTime',
      width: 150,
      title: $t('common.updateTime'),
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'textName',
          nameTitle: $t('system.form.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'auth',
            text: $t('system.org.auth'),
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('common.operation'),
      width: 130,
    },
  ];
}

export const statusColor = {
  '0': 'error',
  '1': 'success',
};
