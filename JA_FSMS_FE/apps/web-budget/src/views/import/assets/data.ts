import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';
import { yearOptions } from '#/utils';

const inputFilterRender = reactive({
  name: 'VxeInput',
});


export function useColumns(): VxeTableGridOptions<any>['columns'] {
  const yearFilterOptions = yearOptions(-10);
  const yearEditOptions = yearOptions(-10, false);

  const yearEditRender = reactive({
    name: 'ASelect',
    props: {
      class: 'w-full',
      allowClear: true,
      options: yearEditOptions,
    },
  });

  const changeStatusOptions = [
    { label: $t('import.assets.unchanged'), value: 'UNCHANGED' },
    { label: $t('import.assets.new'), value: 'NEW' },
    { label: $t('import.assets.modify'), value: 'MODIFY' },
  ];

  return [
    {
      field: 'budgetAssetTypeCode',
      title: $t('import.assets.budgetAssetTypeCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 150,
    },
    {
      field: 'budgetAssetTypeName',
      title: $t('import.assets.budgetAssetTypeName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      minWidth: 150,
    },
    {
      field: 'assetMajorCategoryCode',
      title: $t('import.assets.assetMajorCategoryCode'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      editRender: inputFilterRender,
      slots: { default: 'text' },
      minWidth: 150,
    },
    {
      field: 'assetMajorCategoryName',
      title: $t('import.assets.assetMajorCategoryName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      editRender: inputFilterRender,
      slots: { default: 'text' },
      minWidth: 150,
    },
    {
      field: 'erpAssetType',
      title: $t('import.assets.erpAssetType'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      editRender: inputFilterRender,
      slots: { default: 'text' },
      minWidth: 150,
    },
    {
      field: 'assetTypeName',
      title: $t('import.assets.assetTypeName'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
      editRender: inputFilterRender,
      slots: { default: 'text' },
      minWidth: 150,
    },
    {
      field: 'year',
      title: $t('import.assets.year'),
      // filters: [{ data: dayjs().format('YYYY') }],
      filters: yearFilterOptions,
      editRender: yearEditRender,
      slots: { default: 'text' },
      width: 120,
    },
    {
      field: 'changeStatus',
      title: $t('import.assets.changeStatus'),
      filters: changeStatusOptions,
      slots: { default: 'changeStatus' },
      width: 120,
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
