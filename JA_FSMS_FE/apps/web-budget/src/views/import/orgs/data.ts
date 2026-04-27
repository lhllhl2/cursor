// import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';

const inputFilterRender = reactive({
  name: 'VxeInput',
});

// const booleanEditRender = reactive({
//   name: 'ASelect',
//   props: {
//     class: 'w-full',
//     options: [
//       { label: $t('common.yes'), value: '1' },
//       { label: $t('common.no'), value: null },
//     ],
//   },
// });

const treeSelectEditRender: any = reactive({
  name: 'ATreeSelect',
  props: {
    class: 'w-full',
    treeNodeFilterProp: 'label',
    showSearch: true,
    allowClear: true,
    dropdownMatchSelectWidth: false,
  },
});

export function useColumns(
  manageOrgList: any[],
): VxeTableGridOptions<any>['columns'] {
  treeSelectEditRender.props.treeData = manageOrgList as any[];
  return [
    {
      field: 'ehrCd',
      title: $t('common.code', [$t('import.orgs.ehrOrg')]),
      treeNode: true,
      align: 'left',
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      // cellRender: {
      //   name: 'CellNameAndCode',
      //   attrs: {
      //     nameField: 'ehrNm',
      //     codeField: 'ehrCd',
      //   },
      // },
    },
    {
      field: 'ehrNm',
      title: $t('common.name', [$t('import.orgs.ehrOrg')]),
      filters: [{ data: '' }],
      align: 'left',
      filterRender: inputFilterRender,
      // cellRender: {
      //   name: 'CellNameAndCode',
      //   attrs: {
      //     nameField: 'ehrNm',
      //     codeField: 'ehrCd',
      //   },
      // },
    },
    {
      field: 'orgCd',
      title: $t('import.orgs.manageOrg'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      editRender: treeSelectEditRender,
      slots: { default: 'orgCd' },
      // cellRender: {
      //   name: 'CellNameAndCode',
      //   attrs: {
      //     nameField: 'orgNm',
      //     codeField: 'orgCd',
      //   },
      // },
    },
    {
      field: 'controlLevel',
      width: 130,
      title: $t('import.controlLevel'),
      align: 'center',
      editRender: {},
      // editRender: booleanEditRender,
      slots: { default: 'yes', edit: 'edit_bool' },
    },
    {
      field: 'bzLevel',
      title: $t('import.orgs.bzLevel'),
      width: 130,
      align: 'center',
      editRender: {},
      // editRender: booleanEditRender,
      slots: { default: 'yes', edit: 'edit_bool' },
    },
    {
      field: 'erpDepart',
      title: $t('import.orgs.erpDepart'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      editRender: inputFilterRender,
      slots: { default: 'text' },
      width: 150,
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
