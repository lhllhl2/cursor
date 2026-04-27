<script lang="ts" setup>
import type { ButtonType } from 'ant-design-vue/es/button';
import type {
  TransferDirection,
  TransferItem,
} from 'ant-design-vue/es/transfer';

import type { Recordable } from '@vben/types';

import { onMounted, ref, watch } from 'vue';

import { Button, Space, Transfer } from 'ant-design-vue';

import { searchGroupUserList, userForGroup } from '#/api';
import { $t } from '#/locales';

// 扩展 TransferItem 类型，添加原始数据字段
interface CustomTransferItem extends TransferItem {
  rawData?: Recordable<any>;
}

interface Props {
  /** 用户组ID，用于获取已选用户 */
  groupId?: string;
  /** 已选用户ID列表 */
  modelValue?: string[];
  /** 是否禁用 */
  disabled?: boolean;
  /** 是否显示搜索框 */
  showSearch?: boolean;
  /** 是否显示选择全部按钮 */
  showSelectAll?: boolean;
  /** 穿梭框标题 */
  titles?: [string, string];
  /** 提交按钮配置 */
  submitProps?: {
    [key: string]: any;
    show?: boolean;
    text?: string;
    type?: ButtonType;
  };
  /** 取消按钮配置 */
  cancelProps?: {
    [key: string]: any;
    show?: boolean;
    text?: string;
    type?: ButtonType;
  };
}

interface Emits {
  (e: 'update:modelValue', value: string[]): void;
  (
    e: 'change',
    targetKeys: string[],
    direction: TransferDirection,
    moveKeys: string[],
  ): void;
  (e: 'submit', userIds: string[]): void;
  (e: 'cancel'): void;
}

const props = withDefaults(defineProps<Props>(), {
  groupId: undefined,
  modelValue: () => [],
  disabled: false,
  showSearch: true,
  showSelectAll: true,
  titles: () => [
    $t('system.userGroup.availableUsers'),
    $t('system.userGroup.selectedUsers'),
  ],
  submitProps: () => ({
    show: true,
    text: '确定',
    type: 'primary',
  }),
  cancelProps: () => ({
    show: true,
    text: '取消',
    type: 'default',
  }),
});
const emit = defineEmits<Emits>();
// 响应式数据
const loading = ref(false);
const allUsers = ref<CustomTransferItem[]>([]);
const targetKeys = ref<string[]>([]);

// 初始化时加载所有用户数据
const loadAllUsers = async () => {
  try {
    loading.value = true;
    const response = await userForGroup('');
    const users = response || [];

    allUsers.value = users.map((user: Recordable<any>) => {
      // 构建显示格式：userName displayName(path/officeLocation/post)
      const path = user.path || '';
      const officeLocation = user.officeLocation || '';
      const post = user.post || '';
      const userName = user.userName || '';

      let description = '';
      if (path || officeLocation || post) {
        const parts = [path, officeLocation, post].filter(Boolean);
        description = parts.join('/');
      }

      // 构建显示名称：工号在前，然后是姓名和描述信息
      const displayName = user.displayName || '';
      const namePart = userName ? `${userName} ${displayName}` : displayName;
      const title = description ? `${namePart}(${description})` : namePart;

      return {
        key: user.id,
        title,
        description,
        disabled: false,
        // 保存原始数据，方便后续使用
        rawData: user,
      };
    });
  } catch {
    allUsers.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载已选用户
const loadSelectedUsers = async (groupId: string | undefined) => {
  if (!groupId) return;

  try {
    loading.value = true;
    const response = await searchGroupUserList(groupId);
    const selectedUsers = response || [];

    // 更新已选用户
    const selectedUserIds = selectedUsers.map(
      (user: Recordable<any>) => user.id || user.userId,
    );
    targetKeys.value = selectedUserIds;
    emit('update:modelValue', selectedUserIds);
  } catch (error) {
    console.error('加载已选用户失败:', error);
  } finally {
    loading.value = false;
  }
};

// 监听props变化
watch(
  () => props.modelValue,
  (newValue) => {
    targetKeys.value = [...newValue];
  },
  { immediate: true },
);

watch(
  () => props.groupId,
  (newGroupId) => {
    if (newGroupId) {
      loadSelectedUsers(newGroupId);
    }
  },
  { immediate: true },
);

// 处理穿梭框变化
const handleChange = (
  nextTargetKeys: string[],
  direction: TransferDirection,
  moveKeys: string[],
) => {
  targetKeys.value = nextTargetKeys;
  emit('update:modelValue', nextTargetKeys);
  emit('change', nextTargetKeys, direction, moveKeys);
};

// 处理按钮点击事件
const handleSubmit = () => {
  emit('submit', targetKeys.value);
};

const handleCancel = () => {
  emit('cancel');
};

// 过滤函数 - 用于本地搜索过滤
const filterOption = (inputValue: string, item: TransferItem) => {
  const customItem = item as CustomTransferItem;
  const userName = customItem.rawData?.userName || '';
  return (
    item.title?.includes(inputValue) ||
    item.description?.includes(inputValue) ||
    userName.includes(inputValue)
  );
};

onMounted(() => {
  loadAllUsers();
});

// 暴露方法给父组件
defineExpose({
  loadAllUsers,
  loadSelectedUsers,
  reset: () => {
    targetKeys.value = [];
    emit('update:modelValue', []);
  },
});
</script>

<template>
  <div class="user-transfer-container">
    <!-- 空状态提示 -->
    <div v-if="allUsers.length === 0 && !loading" class="empty-state-wrapper">
      <div class="empty-state">
        <div class="empty-text">{{ $t('system.userGroup.loadingUsers') }}</div>
      </div>
    </div>

    <Transfer
      v-model:target-keys="targetKeys"
      :data-source="allUsers"
      :disabled="disabled"
      :filter-option="filterOption"
      :render="(item) => item.title"
      :loading="loading"
      :show-search="showSearch"
      :show-select-all="showSelectAll"
      :titles="titles"
      class="user-transfer"
      @change="handleChange"
    />

    <!-- 按钮区域 -->
    <div class="button-container">
      <slot name="footer">
        <Space>
          <Button
            v-if="cancelProps.show"
            v-bind="cancelProps"
            @click="handleCancel"
          >
            {{ cancelProps.text }}
          </Button>
          <Button
            v-if="submitProps.show"
            v-bind="submitProps"
            @click="handleSubmit"
          >
            {{ submitProps.text }}
          </Button>
        </Space>
      </slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.user-transfer-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;

  .user-transfer {
    display: flex;
    justify-content: center;
    width: 100%;

    :deep(.ant-transfer-list) {
      min-width: 280px;
      min-height: 400px;
    }
  }

  .button-container {
    width: 100%;
    margin-top: 16px;
    text-align: center;
  }
}
</style>
