import type { ChangeEvent } from 'ant-design-vue/es/_util/EventInterface';

import type { Ref } from 'vue';

import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { $te } from '@vben/locales';

import { $t } from '#/locales';

export const DICT_PREFIX = 'system.dict.';

export function useGridFormSchema() {
  return [
    {
      component: 'Input',
      fieldName: 'code',
      label: $t('system.dict.code'),
    },
  ];
}

export function useColumns<T = SystemDictApi.SystemDict>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'code',
      title: $t('system.dict.code'),
      width: 200,
    },
    {
      field: 'title',
      title: $t('system.dict.name'),
      slots: { default: 'title' },
      width: 200,
    },
    {
      field: 'labelList',
      title: $t('system.dict.options'),
      showOverflow: false,
      align: 'left',
      slots: { default: 'options' },
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'code',
          onClick: onActionClick,
        },
        name: 'CellOperation',
      },
      field: 'operation',
      fixed: 'right',
      title: $t('common.operation'),
      width: 130,
    },
  ];
}

export function useFormSchema(
  titleSuffix: Ref<string | undefined>,
): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'code',
      label: $t('system.dict.code'),
      // rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.dict.name'),
      rules: 'required',
      componentProps() {
        // 不需要处理多语言时就无需这么做
        return {
          addonBefore: DICT_PREFIX,
          addonAfter: titleSuffix.value,
          onChange({ target: { value } }: ChangeEvent) {
            titleSuffix.value =
              value && $te(DICT_PREFIX + value)
                ? $t(DICT_PREFIX + value)
                : undefined;
          },
        };
      },
    },
    {
      component: 'Divider',
      fieldName: 'divider1',
      hideLabel: true,
      formItemClass: 'col-span-24 pb-0',
      renderComponentContent() {
        return {
          default: () => $t('system.dict.options'),
        };
      },
    },
    {
      component: 'Input',
      fieldName: 'options',
      hideLabel: true,
      defaultValue: [],
    },
  ];
}
