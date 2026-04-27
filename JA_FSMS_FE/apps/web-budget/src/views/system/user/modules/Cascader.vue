<script lang="ts" setup>
import type { SystemUserApi } from '#/api';

import { computed, ref, watch } from 'vue';

import { Cascader, message, Modal } from 'ant-design-vue';

import { saveUserGroupByTree } from '#/api';
import { $t } from '#/locales';

interface CascaderOption {
  label: string;
  value: string;
  title?: string;
  children?: CascaderOption[];
}

interface Props {
  row: SystemUserApi.SystemUser;
  treeOptions: any[];
}

interface Emits {
  (e: 'update:groupIds', groupIds: string[]): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const cascaderValue = ref<string[][]>([]);
const loading = ref(false);
const isOpen = ref(false);
// 排序后的选项，只在打开时计算
const sortedCascaderOptions = ref<CascaderOption[]>([]);
// 搜索输入值，手动控制
const searchValue = ref<string>('');
// 过滤后的选项
const filteredOptions = ref<CascaderOption[]>([]);
// 保存打开选择器时的原始值，用于判断是否有修改
const originalCascaderValue = ref<string[][]>([]);

// 转换树形数据为级联选择器需要的格式（不排序，保持原序）
const cascaderOptions = computed<CascaderOption[]>(() => {
  if (!props.treeOptions || props.treeOptions.length === 0) {
    return [];
  }
  return transformTreeData(props.treeOptions);
});

function transformTreeData(data: any[]): CascaderOption[] {
  return data.map((item) => {
    const label = item.id ? item.name || '' : $t(item.name || '');
    return {
      value: item.id ? String(item.id) : item.name || '',
      label,
      title: label,
      children: item.children ? transformTreeData(item.children) : undefined,
    };
  });
}

// 在树中查找节点及其父节点路径
function findNodePath(
  tree: CascaderOption[],
  targetId: string,
  path: string[] = [],
): null | string[] {
  for (const node of tree) {
    const currentPath = [...path, node.value];
    if (node.value === targetId) {
      return currentPath;
    }
    if (node.children && node.children.length > 0) {
      const found = findNodePath(node.children, targetId, currentPath);
      if (found) {
        return found;
      }
    }
  }
  return null;
}

// 根据groupIds构建回显值
function buildCascaderValue(groupIds: string[]): string[][] {
  if (
    !groupIds ||
    groupIds.length === 0 ||
    cascaderOptions.value.length === 0
  ) {
    return [];
  }
  const paths: string[][] = [];
  for (const id of groupIds) {
    const path = findNodePath(cascaderOptions.value, String(id));
    if (path) {
      paths.push(path);
    }
  }
  return paths;
}

// 将级联选择器的路径数组转换为后端需要的树形对象结构
function convertPathsToTree(paths: string[][]): any[] {
  if (!paths?.length) return [];
  const parentMap = new Map<string, any>();
  // 添加子节点到父节点
  const addChild = (parentData: any, childNode: any) => {
    const exists = parentData.children.some(
      (child: any) => String(child.id) === String(childNode.id),
    );
    if (!exists) {
      parentData.children.push({
        id: childNode.id,
        code: childNode.code || '',
        name: childNode.name || '',
        type: childNode.type || '',
        isEnable: true,
      });
    }
  };

  for (const path of paths) {
    if (!path?.[0]) continue;
    const parentName = path[0];
    const parentNode = props.treeOptions.find(
      (item) => !item.id && item.name === parentName,
    );
    if (!parentNode) continue;
    // 初始化父节点
    if (!parentMap.has(parentName)) {
      parentMap.set(parentName, {
        name: parentNode.name || '',
        type: parentNode.type || '',
        children: [],
      });
    }

    const parentData = parentMap.get(parentName);
    const children = parentNode.children || [];
    // 路径长度为1表示选择父级，否则选择具体子级
    if (path.length === 1) {
      children.forEach((childNode: any) => addChild(parentData, childNode));
    } else {
      const childNode = children.find(
        (item: any) => String(item.id) === path[1],
      );
      if (childNode) addChild(parentData, childNode);
    }
  }
  return [...parentMap.values()];
}

// 比较两个级联选择器值是否相同
function isCascaderValueEqual(value1: string[][], value2: string[][]): boolean {
  if (value1.length !== value2.length) return false;
  const sorted1 = value1.map((path) => [...path].sort().join(',')).sort();
  const sorted2 = value2.map((path) => [...path].sort().join(',')).sort();
  return JSON.stringify(sorted1) === JSON.stringify(sorted2);
}

// 保存用户组数据
async function saveUserGroupData(valueArray: string[][]) {
  if (!props.row?.id) return;

  try {
    loading.value = true;
    const treeData = convertPathsToTree(valueArray);
    const selectedIds = new Set<string>();

    for (const path of valueArray) {
      if (path.length === 1) {
        const parentNode = props.treeOptions.find(
          (item) => !item.id && item.name === path[0],
        );
        parentNode?.children?.forEach((child: any) =>
          selectedIds.add(String(child.id)),
        );
      } else if (path.length > 1) {
        const childId = path[path.length - 1];
        if (childId) selectedIds.add(childId);
      }
    }

    await saveUserGroupByTree({
      userId: props.row.id.toString(),
      tree: treeData,
    });
    emit('update:groupIds', [...selectedIds]);
    message.success('保存成功');
    // 保存成功后更新原始值
    originalCascaderValue.value = valueArray.map((path) => [...path]);
  } catch (error) {
    console.error('保存用户组失败:', error);
    throw error;
  } finally {
    loading.value = false;
  }
}

// 监听treeOptions和groupIds变化，页面刷新时重新回显
watch(
  [() => cascaderOptions.value.length, () => props.row?.groupIds],
  () => {
    if (cascaderOptions.value.length > 0 && props.row?.groupIds?.length) {
      cascaderValue.value = buildCascaderValue(props.row.groupIds);
    } else if (!props.row?.groupIds?.length) {
      cascaderValue.value = [];
    }
  },
  { immediate: true, deep: true },
);

// 递归排序：将已选中的项排在其父级下的最前面
function sortOptionsBySelectedIds(
  options: CascaderOption[],
  selectedIdsSet: Set<string>,
): CascaderOption[] {
  return options
    .map((option) => ({
      ...option,
      children: option.children
        ? sortOptionsBySelectedIds(option.children, selectedIdsSet)
        : undefined,
    }))
    .sort((a, b) => {
      const aSelected = selectedIdsSet.has(a.value);
      const bSelected = selectedIdsSet.has(b.value);
      if (aSelected === bSelected) return 0;
      return aSelected ? -1 : 1;
    });
}

// 更新排序后的选项
function updateSortedOptions() {
  const groupIds = props.row?.groupIds;
  if (groupIds?.length) {
    const selectedIdsSet = new Set<string>(groupIds.map(String));
    sortedCascaderOptions.value = sortOptionsBySelectedIds(
      cascaderOptions.value,
      selectedIdsSet,
    );
  } else {
    sortedCascaderOptions.value = cascaderOptions.value;
  }
  // 更新后重新触发过滤
  filteredOptions.value = searchValue.value
    ? filterOptions(sortedCascaderOptions.value, searchValue.value.trim())
    : sortedCascaderOptions.value;
}

function filterOptions(
  options: CascaderOption[],
  searchText: string,
): CascaderOption[] {
  if (!searchText) return options;

  const lowerSearchText = searchText.toLowerCase();

  return options
    .map((option): CascaderOption | null => {
      const matchesSearch = option.label
        .toLowerCase()
        .includes(lowerSearchText);
      const filteredChildren = option.children
        ? filterOptions(option.children, searchText)
        : undefined;

      if (matchesSearch || (filteredChildren && filteredChildren.length > 0)) {
        return {
          ...option,
          children: filteredChildren,
        };
      }
      return null;
    })
    .filter((option): option is CascaderOption => option !== null);
}

// 监听搜索值变化，手动过滤选项
watch(
  [
    searchValue,
    () => (isOpen.value ? sortedCascaderOptions.value : cascaderOptions.value),
  ],
  ([searchText, options]) => {
    filteredOptions.value =
      !searchText || !searchText.trim()
        ? options
        : filterOptions(options, searchText.trim());
  },
  { immediate: true, deep: true },
);

// 搜索值变化处理
function handleSearch(value: string) {
  searchValue.value = value || '';
}

// 级联选择器值变化处理
function handleChange(value: any) {
  const arr = Array.isArray(value) ? value : [];
  if (arr.length === 0) {
    cascaderValue.value = [];
  } else if (Array.isArray(arr[0])) {
    cascaderValue.value = arr as string[][];
  } else {
    cascaderValue.value = [arr as string[]];
  }
  if (isOpen.value) updateSortedOptions();
  // 选择后不清空搜索值，保持搜索状态
}

// 选择器打开/关闭处理
async function handlePopupVisibleChange(visible: boolean) {
  isOpen.value = visible;
  if (visible) {
    // 打开时保存当前值作为原始值
    originalCascaderValue.value = cascaderValue.value.map((path) => [...path]);
    updateSortedOptions();
    return;
  }
  // 关闭时清空搜索值
  searchValue.value = '';

  // 关闭时检查是否有修改，如果有修改则弹出确认弹窗
  if (!props.row?.id) return;
  const currentValue = cascaderValue.value;
  const hasChanged = !isCascaderValueEqual(
    currentValue,
    originalCascaderValue.value,
  );

  if (hasChanged) {
    Modal.confirm({
      title: '确认修改',
      content: '您已修改用户组信息，是否确认保存？',
      okText: '确认',
      cancelText: '取消',
      async onOk() {
        await saveUserGroupData(currentValue);
      },
      onCancel() {
        // 取消时恢复原始值
        cascaderValue.value = originalCascaderValue.value.map((path) => [
          ...path,
        ]);
      },
    });
  }
}
</script>

<template>
  <Cascader
    v-model:value="cascaderValue"
    v-model:search-value="searchValue"
    :options="
      filteredOptions.length > 0 || searchValue
        ? filteredOptions
        : isOpen
          ? sortedCascaderOptions
          : cascaderOptions
    "
    :loading="loading"
    :multiple="true"
    :change-on-select="false"
    :max-tag-count="1"
    :allow-clear="false"
    :show-search="true"
    popup-class-name="user-group-cascader-dropdown"
    class="user-group-cascader"
    @change="handleChange"
    @popup-visible-change="handlePopupVisibleChange"
    @search="handleSearch"
  />
</template>

<style scoped>
.user-group-cascader {
  width: 100%;
}

:deep(.ant-select-selector) {
  height: 32px;
  overflow: hidden;
}

:deep(.ant-select-selection-overflow) {
  overflow: hidden;
}

:deep(.ant-select-selection-item) {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

<style>
.user-group-cascader-dropdown .ant-cascader-menu {
  width: 210px;
}

.user-group-cascader-dropdown .ant-cascader-menu-item {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
