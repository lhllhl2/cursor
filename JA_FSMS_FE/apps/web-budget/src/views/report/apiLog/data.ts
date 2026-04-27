import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { reactive } from 'vue';

import { $t } from '#/locales';

/** 筛选输入框渲染器 */
const inputFilterRender = reactive({
  name: 'VxeInput',
});

/** 表格列配置 */
export function useColumns(): VxeTableGridOptions<any>['columns'] {
  return [
    {
      field: 'docNo',
      title: $t('report.apiLog.docNo'),
      width: 120,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'userIp',
      title: $t('report.apiLog.userIp'),
      width: 120,
    },
    {
      field: 'interfaceName',
      title: $t('report.apiLog.interfaceName'),
      align: 'left',
      width: 120,
      filters: [{ data: '' }],
      filterRender: inputFilterRender,
    },
    {
      field: 'requestUrl',
      title: $t('report.apiLog.requestUrl'),
      align: 'left',
      width: 250,
    },
    {
      field: 'status',
      title: $t('report.apiLog.status'),
      width: 100,
      slots: { default: 'status' },
    },
    {
      field: 'responseResult',
      title: $t('report.apiLog.responseResult'),
      align: 'left',
    },
  ];
}
