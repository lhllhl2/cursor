import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';

const inputFilterRender = reactive({
  name: 'VxeInput',
});

// const booleanEditRender = reactive({
//   name: 'ASelect',
//   props: {
//     class: 'w-[100px]',
//     options: [
//       { label: $t('common.yes'), value: '1' },
//       { label: $t('common.no'), value: null },
//     ],
//   },
// });

export function useColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'cust1Cd',
      title: $t('common.code', [$t('import.subjects.cust1')]),
      align: 'center',
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      // cellRender: {
      //   name: 'CellNameAndCode',
      //   attrs: {
      //     nameField: 'cust1Nm',
      //     codeField: 'cust1Cd',
      //   },
      // },
    },
    {
      field: 'cust1Nm',
      title: $t('common.name', [$t('import.subjects.cust1')]),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'acctKey',
      title: $t('import.subjects.name'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      cellRender: {
        name: 'CellMerge',
        attrs: {
          merges: ['acctCd', 'acctNm'],
        },
      },
    },
    {
      field: 'erpAcctKey',
      title: $t('import.subjects.erpAcct'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      cellRender: {
        name: 'CellMerge',
        attrs: {
          merges: ['erpAcctCd', 'erpAcctNm'],
        },
      },
    },
    {
      field: 'controlAcctCd',
      title: $t('import.subjects.controlAcctCd'),
      align: 'center',
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 180,
    },
    {
      field: 'controlAcctNm',
      title: $t('import.subjects.controlAcctNm'),
      align: 'center',
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      width: 220,
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
      width: 150,
    },
    {
      field: 'updateTime',
      title: $t('common.updateTime'),
      width: 150,
    },
  ];
}
