import type { Recordable } from '@vben/types';

import { h } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { VxeTableInputFilter } from '../component';

/**
 * 注册 VXE Table 表头的列渲染器
 */
export function setupHeaderRenderers(vxeUI: any) {
  // 超链接
  vxeUI.renderer.add('HeaderLink', {
    renderTableHeader(renderOpts: any, renderParams: any) {
      const { column } = renderParams;
      const { attrs } = renderOpts;
      return h(
        'a',
        {
          href: attrs.path,
          target: '_blank',
        },
        column.title,
      );
    },
  });

  // 带图标按钮的表头
  vxeUI.renderer.add('HeaderButton', {
    renderTableHeader(renderOpts: any, renderParams: any) {
      const { column } = renderParams;
      const { attrs } = renderOpts;
      // 获取配置参数
      const icon = attrs?.icon || 'mdi:information-outline'; // 默认图标
      const title = attrs?.title || ''; // 原生 title 提示文本
      const iconSize = attrs?.iconSize || '16px'; // 图标大小
      const iconColor = attrs?.iconColor || '#000'; // 图标颜色

      // 创建图标按钮
      const iconButton = h(IconifyIcon, {
        icon,
        class: 'vxt-header-btn-icon',
        style: {
          fontSize: iconSize,
          color: iconColor,
        },
        onClick: (e: Event) => {
          e.stopPropagation(); // 阻止事件冒泡

          // 切换 active 类
          const target = e.target as HTMLElement;
          target.classList.toggle('active');
          const isActive = target.classList.contains('active');
          // 调用用户自定义的 onClick 回调
          attrs?.onClick?.(renderParams, isActive);
        },
      });

      // 返回表头内容：标题 + 图标按钮
      return h(
        'span',
        {
          class: 'vxt-header-btn',
        },
        [
          h('span', column.title), // 列标题
          h(
            'span',
            {
              title,
            },
            iconButton,
          ), // 图标按钮（包裹在 span 中）
        ],
      );
    },
  });

  vxeUI.renderer.add('InputFilter', {
    // 不显示底部按钮，使用自定义的按钮
    showTableFilterFooter: false,
    // 自定义筛选模板
    renderTableFilter(renderOpts: any, renderParams: any) {
      return h(VxeTableInputFilter, {
        renderOpts,
        renderParams,
      });
    },

    tableFilterResetMethod(params) {
      const { options } = params;
      options.forEach((option) => {
        option.data = {
          text: '',
          type: 'has',
        };
      });
    },
    // 自定义重置筛选复原方法（当未点击确认时，该选项将被恢复为默认值）
    tableFilterRecoverMethod({ option }) {
      option.data = {
        text: '',
        type: 'has',
      };
    },
    // 自定义筛选方法
    tableFilterMethod(params) {
      const { option, row, column } = params;
      const { data } = option;
      const cellValue = row[column.field];
      if (cellValue) {
        return cellValue.includes(data);
      }
      return false;
    },
  });
}

export function getFilterParams(filters: any) {
  const params: Recordable<any> = {};
  filters.forEach((item: any) => {
    if (item.datas[0]) {
      params[item.field] =
        item.datas[0].type === 'NAN' ? item.datas[0].type : item.datas[0].text;
    } else if (item.values[0]) {
      params[item.field] = item.values[0];
    }
  });
  return params;
}
