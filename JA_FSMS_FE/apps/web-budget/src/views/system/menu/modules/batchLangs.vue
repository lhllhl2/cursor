<script setup lang="ts">
import type { Recordable } from '@vben/types';

import type { VbenFormSchema } from '#/adapter/form';

import { h, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { getPopupContainer } from '@vben/utils';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getLangs, updateLangs } from '#/api/system/langs';
import { getMenuList } from '#/api/system/menu';
import { $t } from '#/locales';

const emit = defineEmits<{
  success: [];
}>();

const selectedMenuId = ref<string>();
const jsonText = ref<string>('');
const loading = ref(false);

const schema: VbenFormSchema[] = [
  {
    component: 'ApiTreeSelect',
    componentProps: {
      class: 'w-full',
      api: getMenuList,
      filterTreeNode(input: string, node: Recordable<any>) {
        if (!input || input.length === 0) {
          return true;
        }
        const title: string = node.meta?.title ?? '';
        if (!title) return false;
        return title.includes(input) || $t(title).includes(input);
      },
      getPopupContainer,
      labelField: 'meta.title',
      showSearch: true,
      valueField: 'id',
      childrenField: 'children',
      placeholder: '选择菜单',
      allowClear: true,
      onChange: onMenuChange,
    },
    fieldName: 'menuId',
    label: '',
    renderComponentContent() {
      return {
        title({ label, meta }: { label: string; meta: Recordable<any> }) {
          const coms = [];
          if (!label) return '';
          if (meta?.icon) {
            coms.push(h(IconifyIcon, { class: 'size-4', icon: meta.icon }));
          }
          coms.push(h('span', { class: '' }, $t(label || '')));
          return h('div', { class: 'flex items-center gap-1' }, coms);
        },
      };
    },
  },
  {
    component: 'Textarea',
    componentProps: {
      rows: 16,
      placeholder: '选择菜单后，将在此显示多语言配置（JSON 格式）',
      class: 'font-mono text-sm',
      style: { height: '400px' },
    },
    fieldName: 'jsonData',
    label: '',
    help: '',
  },
];

const [Form, formApi] = useVbenForm({
  layout: 'vertical',
  commonConfig: {
    colon: true,
  },
  schema,
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    await onSave();
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      // 重置状态
      formApi.resetForm();
      selectedMenuId.value = undefined;
      jsonText.value = '';
    }
  },
  title: '多语言配置',
});

/**
 * 菜单选择变化
 */
async function onMenuChange(menuId: string) {
  selectedMenuId.value = menuId;

  if (!menuId) {
    formApi.setFieldValue('jsonData', '');
    return;
  }

  loading.value = true;
  try {
    const data = await getLangs({ menuId });

    // 将数组格式转换为 JSON 对象格式
    const jsonData: Record<string, Record<string, string>> = {};
    data.forEach((item: any) => {
      const { title, ...langs } = item;
      if (title) {
        jsonData[title] = langs;
      }
    });

    // 格式化 JSON（缩进 2 空格）
    const jsonString = JSON.stringify(jsonData, null, 2);
    formApi.setFieldValue('jsonData', jsonString);
  } catch {
    message.error('获取多语言数据失败');
    formApi.setFieldValue('jsonData', '');
  } finally {
    loading.value = false;
  }
}

/**
 * 保存
 */
async function onSave() {
  const values = await formApi.getValues();
  if (!values.menuId) {
    message.warning('请先选择菜单');
    return;
  }

  if (!values.jsonData || !values.jsonData.trim()) {
    message.warning('多语言配置不能为空');
    return;
  }

  // 校验 JSON 格式
  let jsonData: Record<string, Record<string, string>>;
  try {
    jsonData = JSON.parse(values.jsonData);
  } catch {
    message.error('JSON 格式错误，请检查后重试');
    return;
  }

  // 校验数据结构
  if (typeof jsonData !== 'object' || Array.isArray(jsonData)) {
    message.error('JSON 格式错误：根节点必须是对象');
    return;
  }

  loading.value = true;
  try {
    await updateLangs({
      menuId: values.menuId,
      jsonData,
    });
    message.success($t('ui.actionMessage.operationSuccess'));
    modalApi.close();
    emit('success');
  } catch {
    message.error('保存失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

defineExpose({
  modalApi,
});
</script>

<template>
  <Modal class="w-full max-w-[800px]">
    <Form class="mx-4" />
  </Modal>
</template>
