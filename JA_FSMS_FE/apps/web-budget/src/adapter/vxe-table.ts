import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';
import type { Recordable } from '@vben/types';

import type { ComponentType } from './component';

import {
  setupVbenVxeTable,
  useVbenVxeGrid as useGrid,
} from '@vben/plugins/vxe-table';

import VxeUIPluginRenderAntd from '@vxe-ui/plugin-render-antd';
import {
  Checkbox as ACheckbox,
  Select as ASelect,
  TreeSelect as ATreeSelect,
} from 'ant-design-vue';

import { useVbenForm } from './form';
import { setupCellRenderers, setupHeaderRenderers } from './modules';

import './vxe-table.less';

setupVbenVxeTable({
  configVxeTable: (vxeUI) => {
    vxeUI.setConfig({
      grid: {
        align: 'center',
        headerAlign: 'center',
        border: true,
        columnConfig: {
          resizable: true,
        },
        formConfig: {
          // 全局禁用vxe-table的表单配置，使用formOptions
          enabled: false,
        },
        rowConfig: {
          keyField: 'id',
          isHover: true,
        },
        toolbarConfig: {
          custom: true,
          refresh: true,
          zoom: true,
        },
        minHeight: 250,
        proxyConfig: {
          autoLoad: true,
          response: {
            result: 'list',
            total: 'total',
            list: '',
          },
          showActiveMsg: true,
          showResponseMsg: false,
        },
        round: true,
        showOverflow: true,
        size: 'small',
      } as VxeTableGridOptions,
    });

    // 注册所有列渲染器
    setupCellRenderers(vxeUI);

    // 注册所有表头渲染器
    setupHeaderRenderers(vxeUI);

    // 注册vxe-table的antd渲染器
    VxeUIPluginRenderAntd.component(ACheckbox);
    VxeUIPluginRenderAntd.component(ASelect);
    VxeUIPluginRenderAntd.component(ATreeSelect);
    vxeUI.use(VxeUIPluginRenderAntd);
  },
  useVbenForm,
});

export const useVbenVxeGrid = <T extends Record<string, any>>(
  ...rest: Parameters<typeof useGrid<T, ComponentType>>
) => {
  // TO DO: 如何响应式更新defaultFormOptions
  const defaultFormOptions: any = {
    wrapperClass: `grid-cols-1 md:grid-cols-3 lg:grid-cols-4`,
    submitOnEnter: true,
  };
  rest.forEach((option) => {
    if (option.formOptions) {
      option.formOptions = {
        ...defaultFormOptions,
        ...option.formOptions,
      };
    }
    if (option.gridOptions?.exportConfig || option.gridOptions?.importConfig) {
      option.gridOptions.toolbarConfig = {
        ...option.gridOptions?.toolbarConfig,
        ...(option.gridOptions?.exportConfig && { export: true }),
        ...(option.gridOptions?.importConfig && { import: true }),
      };
    }
  });

  return useGrid<T, ComponentType>(...rest);
};

/**
 * 将 VXE Table 的筛选参数转换为 API 请求参数
 * @param filters VXE Table 的筛选对象数组
 * @returns 转换后的请求参数对象
 */
export function getFilterParams(filters: any[]): Recordable<any> {
  const params: Recordable<any> = {};
  filters.forEach((item: any) => {
    if (item.datas?.[0]) {
      params[item.field] =
        item.datas[0].type === 'NAN'
          ? item.datas[0].type
          : item.datas[0].text || item.datas[0];
    } else if (item.values?.[0]) {
      params[item.field] = item.values;
    }
  });
  return params;
}

export type OnActionClickParams<T = Recordable<any>> = {
  code: string;
  row: T;
};
export type OnActionClickFn<T = Recordable<any>> = (
  params: OnActionClickParams<T>,
) => void;
export type * from '@vben/plugins/vxe-table';
