import type { Directive } from 'vue';

import type { Recordable } from '@vben/types';

import { h, resolveDirective, withDirectives } from 'vue';
import { RouterLink } from 'vue-router';

import { IconifyIcon } from '@vben/icons';
import { $te } from '@vben/locales';
import { get, isFunction, isString } from '@vben/utils';

import { objectOmit } from '@vueuse/core';
import {
  Button,
  Image,
  Popconfirm,
  Switch,
  Tag,
  Tooltip,
} from 'ant-design-vue';

import { $t } from '#/locales';
import { formatMoney } from '#/utils';

/**
 * 注册 VXE Table 的列渲染器
 */
export function setupCellRenderers(vxeUI: any) {
  /**
   * 解决vxeTable在热更新时可能会出错的问题
   */
  vxeUI.renderer.forEach((_item: any, key: string) => {
    if (key.startsWith('Cell')) {
      vxeUI.renderer.delete(key);
    }
  });
  // 审计信息渲染器（创建/更新信息合并显示 - 带 Tooltip）
  vxeUI.renderer.add('CellAcitonInfo', {
    renderTableDefault(renderOpts: any, params: any) {
      const { row } = params;
      const { attrs } = renderOpts;

      // 获取创建和更新信息的字段配置
      const creatorFields = attrs?.creator || ['creator', 'createTime'];
      const updaterFields = attrs?.updater || ['updater', 'updateTime'];

      // 提取字段值
      const creator = get(row, creatorFields[0]) || '-';
      const createTime = get(row, creatorFields[1]) || '-';
      const updater = get(row, updaterFields[0]) || '-';
      const updateTime = get(row, updaterFields[1]) || '-';

      // 创建信息组
      const createInfo = h('div', { class: 'flex items-center' }, [
        h(IconifyIcon, {
          icon: 'lucide:user-plus',
          class: 'text-blue-500 mr-1',
          style: { fontSize: '14px', flexShrink: '0' },
        }),
        h('span', { class: 'text-gray-900 mr-1' }, creator),
        h('span', { class: 'text-gray-500' }, createTime),
      ]);

      // 更新信息组
      const updateInfo = h('div', { class: 'flex items-center' }, [
        h(IconifyIcon, {
          icon: 'lucide:user-check',
          class: 'text-green-500 mr-1',
          style: { fontSize: '14px', flexShrink: '0' },
        }),
        h('span', { class: 'text-gray-900 mr-1' }, updater),
        h('span', { class: 'text-gray-500' }, updateTime),
      ]);

      return h('div', { class: 'flex items-center text-xs' }, [
        // 创建信息（带 Tooltip）
        h(Tooltip, { title: '创建信息' }, { default: () => createInfo }),

        // 分隔符
        h('span', { class: 'text-gray-400 mx-2' }, '|'),

        // 更新信息（带 Tooltip）
        h(Tooltip, { title: '更新信息' }, { default: () => updateInfo }),
      ]);
    },
  });
  // 列合并渲染器
  vxeUI.renderer.add('CellMerge', {
    renderTableDefault(renderOpts: any, params: any) {
      const { row } = params;
      const { attrs } = renderOpts;

      if (!attrs?.merges || !Array.isArray(attrs.merges)) {
        return h('span', '');
      }

      const elements: any[] = [];

      attrs.merges.forEach((field: string, index: number) => {
        const value = get(row, field) || '';

        // 偶数索引（0, 2, 4...）添加特殊样式
        const isEven = index % 2 === 0;
        const className = isEven ? 'font-semibold text-gray-500 mr-1' : '';

        elements.push(h('span', { class: className }, value));
      });

      return h('span', elements);
    },
  });

  // 图片渲染器
  vxeUI.renderer.add('CellImage', {
    renderTableDefault(_renderOpts: any, params: any) {
      const { column, row } = params;
      return h(Image, { src: row[column.field] });
    },
  });

  // 金额渲染器（带颜色）
  vxeUI.renderer.add('CellMoney', {
    renderTableDefault(_renderOpts: any, params: any) {
      const { column, row } = params;
      const value = get(row, column.field);
      // value > 0 =>#52c41a
      // value < 0 =>#ff4d4f
      // value = 0 =>#323639
      function getColor(value: number) {
        if (value > 0) return '#52c41a';
        if (value < 0) return '#ff4d4f';
        return '#323639';
      }
      const color = getColor(value);
      return h('span', { style: { color } }, formatMoney(value));
    },
  });

  // 链接渲染器
  vxeUI.renderer.add('CellLink', {
    renderTableDefault(renderOpts: any, params: any) {
      const { attrs } = renderOpts;
      const { column, row } = params;
      const value = get(row, column.field);
      const type = attrs?.type;
      if (type === 'router') {
        const name = attrs?.name;
        let query = {};
        attrs?.query.forEach((q) => {
          query[q] = get(row, q);
        });
        query = {
          ...query,
          ...attrs.value,
        };
        return h(
          RouterLink,
          {
            to: { name, query },
            style: {
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px',
              textDecoration: 'none',
              color: '#1890ff',
            },
            onClick: (e: any) => {
              e.preventDefault();
              attrs?.onClick?.(row);
            },
          },
          {
            default: () => [
              h(IconifyIcon, {
                icon: 'carbon:launch',
                style: { fontSize: '14px' },
              }),
              value,
            ],
          },
        );
      } else {
        const url = attrs?.nameField ? get(row, attrs.nameField) : value;
        const showTooltip = attrs?.showTooltip !== false; // 默认显示Tooltip，除非明确设置为false
        const tooltipPlacement = attrs?.tooltipPlacement || 'right';
        const tooltipMaxWidth = attrs?.tooltipMaxWidth || '600px';

        const linkElement = h(
          'a',
          {
            onClick: () => {
              window.open(url, '_blank');
            },
            style: {
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px',
              textDecoration: 'none',
              color: '#1890ff',
            },
          },
          {
            default: () => [
              h(IconifyIcon, {
                icon: 'carbon:link',
                style: { fontSize: '14px' },
              }),
              value,
            ],
          },
        );

        // 根据配置决定是否显示Tooltip
        return showTooltip
          ? h(
              Tooltip,
              {
                title: url,
                placement: tooltipPlacement,
                overlayStyle: {
                  maxWidth: tooltipMaxWidth,
                  wordBreak: 'break-all',
                },
              },
              {
                default: () => linkElement,
              },
            )
          : linkElement;
      }
    },
  });

  // Tag 渲染器
  vxeUI.renderer.add('CellTag', {
    renderTableDefault({ options, props }: any, { column, row }: any) {
      const value = get(row, column.field);
      const tagOptions = options ?? [
        { color: 'success', label: $t('common.enabled'), value: '1' },
        { color: 'error', label: $t('common.disabled'), value: '0' },
      ];
      const tagItem = tagOptions.find((item: any) => item.value === value);
      return h(
        Tag,
        {
          ...props,
          ...objectOmit(tagItem ?? {}, ['label']),
        },
        { default: () => tagItem?.label ?? value },
      );
    },
  });

  // 菜单 Tag 渲染器
  vxeUI.renderer.add('MenuTag', {
    renderTableDefault({ options, props }: any, { column, row }: any) {
      const value = get(row, column.field);
      const tagOptions = options ?? [
        { color: 'success', label: $t('common.enabled'), value: 1 },
        { color: 'error', label: $t('common.disabled'), value: 0 },
      ];
      const tagItem = tagOptions.find((item: any) => item.value === value);
      return h(
        Tag,
        {
          ...props,
          ...objectOmit(tagItem ?? {}, ['label']),
        },
        { default: () => tagItem?.label ?? value },
      );
    },
  });

  // Switch 渲染器
  vxeUI.renderer.add('CellSwitch', {
    renderTableDefault({ attrs, props }: any, { column, row }: any) {
      const loadingKey = `__loading_${column.field}`;
      const finallyProps = {
        checkedChildren: $t('common.enabled'),
        checkedValue: 1,
        unCheckedChildren: $t('common.disabled'),
        unCheckedValue: 0,
        ...props,
        checked: row[column.field],
        loading: row[loadingKey] ?? false,
        'onUpdate:checked': onChange,
      };
      async function onChange(newVal: any) {
        row[loadingKey] = true;
        try {
          const result = await attrs?.beforeChange?.(newVal, row);
          if (result !== false) {
            row[column.field] = newVal;
          }
        } finally {
          row[loadingKey] = false;
        }
      }
      return h(Switch, finallyProps);
    },
  });

  // 字典渲染器
  vxeUI.renderer.add('DictCell', {
    renderTableDefault(_renderOpts: any, { column, row }: any) {
      const value = get(row, `${column.field}Des`);
      return value && $te(value) ? $t(value) : value;
    },
  });

  // 操作按钮渲染器
  vxeUI.renderer.add('CellOperation', {
    renderTableDefault({ attrs, options, props }: any, { column, row }: any) {
      const defaultProps = { size: 'small', type: 'link', ...props };
      let align = 'end';
      switch (column.align) {
        case 'center': {
          align = 'center';
          break;
        }
        case 'left': {
          align = 'start';
          break;
        }
        default: {
          align = 'end';
          break;
        }
      }
      const presets: Recordable<Recordable<any>> = {
        delete: {
          danger: true,
          text: $t('common.delete'),
        },
        edit: {
          text: $t('common.edit'),
        },
      };
      const operations: Array<Recordable<any>> = (options || ['edit', 'delete'])
        .map((opt: any) => {
          if (isString(opt)) {
            return presets[opt]
              ? { code: opt, ...presets[opt], ...defaultProps }
              : {
                  code: opt,
                  text: $te(`common.${opt}`) ? $t(`common.${opt}`) : opt,
                  ...defaultProps,
                };
          } else {
            return { ...defaultProps, ...presets[opt.code], ...opt };
          }
        })
        .map((opt: any) => {
          const optBtn: Recordable<any> = {};
          Object.keys(opt).forEach((key) => {
            optBtn[key] = isFunction(opt[key]) ? opt[key](row) : opt[key];
          });
          return optBtn;
        })
        .filter((opt: any) => opt.show !== false);

      function renderBtn(opt: Recordable<any>, listen = true) {
        const accessDirective: Directive[] = opt.access
          ? [resolveDirective('access'), opt.access]
          : [];
        return withDirectives(
          h(
            Button,
            {
              ...props,
              ...opt,
              icon: undefined,
              onClick: listen
                ? () =>
                    attrs?.onClick?.({
                      code: opt.code,
                      row,
                    })
                : undefined,
            },
            {
              default: () => {
                const content = [];
                if (opt.icon) {
                  content.push(
                    h(IconifyIcon, { class: 'size-5', icon: opt.icon }),
                  );
                }
                content.push(opt.text);
                return content;
              },
            },
          ),
          [accessDirective as any],
        );
      }

      function renderConfirm(opt: Recordable<any>) {
        let viewportWrapper: HTMLElement | null = null;
        return h(
          Popconfirm,
          {
            /**
             * 当popconfirm用在固定列中时，将固定列作为弹窗的容器时可能会因为固定列较窄而无法容纳弹窗
             * 将表格主体区域作为弹窗容器时又会因为固定列的层级较高而遮挡弹窗
             * 将body或者表格视口区域作为弹窗容器时又会导致弹窗无法跟随表格滚动。
             * 鉴于以上各种情况，一种折中的解决方案是弹出层展示时，禁止操作表格的滚动条。
             * 这样既解决了弹窗的遮挡问题，又不至于让弹窗随着表格的滚动而跑出视口区域。
             */
            getPopupContainer(el: HTMLElement) {
              viewportWrapper = el.closest('.vxe-table--viewport-wrapper');
              return document.body;
            },
            placement: 'topLeft',
            title: $t('ui.actionTitle.delete', [attrs?.nameTitle || '']),
            ...props,
            ...opt,
            icon: undefined,
            onOpenChange: (open: boolean) => {
              // 当弹窗打开时，禁止表格的滚动
              if (open) {
                viewportWrapper?.style.setProperty('pointer-events', 'none');
              } else {
                viewportWrapper?.style.removeProperty('pointer-events');
              }
            },
            onConfirm: () => {
              attrs?.onClick?.({
                code: opt.code,
                row,
              });
            },
          },
          {
            default: () => renderBtn({ ...opt }, false),
            description: () =>
              h(
                'div',
                { class: 'truncate' },
                $t('ui.actionMessage.deleteConfirm', [
                  row[attrs?.nameField || 'name'],
                ]),
              ),
          },
        );
      }

      const btns = operations
        .filter((opt: any) => !opt.hide)
        .map((opt: any) =>
          opt.code === 'delete' ? renderConfirm(opt) : renderBtn(opt),
        );
      return h(
        'div',
        {
          class: 'flex table-operations',
          style: { justifyContent: align },
        },
        btns,
      );
    },
  });
}
