import type { VxeGridPropTypes } from '#/adapter/vxe-table';
import type { EhrMappingApi } from '#/api';

import { reactive } from 'vue';

import { $t } from '#/locales';

/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

/**
 * 表格列配置
 */
export function useColumns(): VxeGridPropTypes.Columns<EhrMappingApi.EhrMappingItem> {
  return [
    {
      field: 'ehrCd',
      title: $t('report.ehrMapping.ehrCd'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'erpDepart',
      title: $t('report.ehrMapping.erpDepart'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'ehrNm',
      title: $t('report.ehrMapping.ehrNm'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'budgetOrgCd',
      title: $t('report.ehrMapping.budgetOrgCd'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'budgetOrgNm',
      title: $t('report.ehrMapping.budgetOrgNm'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'budgetEhrCd',
      title: $t('report.ehrMapping.budgetEhrCd'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'budgetEhrNm',
      title: $t('report.ehrMapping.budgetEhrNm'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'controlEhrCd',
      title: $t('report.ehrMapping.controlEhrCd'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'controlEhrNm',
      title: $t('report.ehrMapping.controlEhrNm'),
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
  ];
}
