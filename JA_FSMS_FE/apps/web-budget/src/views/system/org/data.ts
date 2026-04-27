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
          types: [3],
        },
        labelField: 'name',
        valueField: 'id',
      },
    },
    // {
    //   component: 'ApiSelect',
    //   fieldName: 'themes',
    //   label: $t('system.dict.reportTheme'),
    //   componentProps: {
    //     allowClear: true,
    //     mode: 'multiple',
    //     api: fetchDictByCode,
    //     params: {
    //       code: DIC_ENUM.REPORT_THEME,
    //     },
    //   },
    // },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    // {
    //   component: 'ApiSelect',
    //   fieldName: 'type',
    //   label: $t('system.dict.orgType'),
    //   componentProps: {
    //     api: fetchDictByCode,
    //     class: 'w-full',
    //     params: {
    //       code: DIC_ENUM.ORG_TYPE,
    //     },
    //   },
    //   defaultValue: 'LE',
    // },
    {
      component: 'Input',
      fieldName: 'name',
      label: $t('system.org.name'),
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: $t('system.org.code'),
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
      field: 'name',
      title: $t('system.org.name'),
      treeNode: true,
      minWidth: 200,
      cellRender: {
        name: 'CellMerge',
        attrs: {
          merges: ['code', 'name'],
        },
      },
    },
    // {
    //   field: 'code',
    //   title: $t('system.org.code'),
    // },
    // {
    //   field: 'pName',
    //   title: $t('system.org.pName'),
    //   width: 200,
    //   cellRender: {
    //     name: 'DictCell',
    //   },
    // },
    // {
    //   field: 'type',
    //   width: 150,
    //   title: $t('system.form.type'),
    //   cellRender: {
    //     name: 'DictCell',
    //   },
    // },
    // {
    //   field: 'contactPerson',
    //   minWidth: 100,
    //   title: $t('system.org.contactPerson'),
    // },
    // {
    //   field: 'contactPhone',
    //   minWidth: 100,
    //   title: $t('system.org.contactPhone'),
    // },
    // {
    //   field: 'status',
    //   width: 150,
    //   title: $t('common.status'),
    //   cellRender: {
    //     name: 'MenuTag',
    //   },
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
            // hide: (row: SystemOrgApi.SystemOrg) => {
            //   return row.children && row.children.length > 0;
            // },
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
