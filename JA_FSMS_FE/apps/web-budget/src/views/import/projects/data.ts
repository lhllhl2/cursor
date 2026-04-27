import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';

const inputFilterRender = reactive({
  name: 'VxeInput',
});
export function useColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'prjCd',
      title: $t('common.code', [$t('import.projects.name')]),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      // cellRender: {
      //   name: 'CellNameAndCode',
      //   attrs: {
      //     nameField: 'prjNm',
      //     codeField: 'prjCd',
      //   },
      // },
    },
    {
      field: 'prjNm',
      title: $t('common.name', [$t('import.projects.name')]),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'controlLevel',
      title: $t('import.controlLevel'),
      align: 'center',
      editRender: {},
      slots: { default: 'yes', edit: 'edit_bool' },
      width: 200,
    },
    {
      field: 'updater',
      title: $t('common.updater'),
    },
    {
      field: 'updateTime',
      title: $t('common.updateTime'),
    },
  ];
}
