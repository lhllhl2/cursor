# Vue 组件开发最佳实践

## 组件结构

### Script Setup 语法

项目统一使用 `<script setup lang="ts">` 语法：

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// 类型导入
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

// 组件导入
import { Page } from '@vben/common-ui';

// API 导入
import { getUserList } from '#/api';

// 工具导入
import { $t } from '#/locales';

// 响应式数据
const loading = ref(false);
const userList = ref<SystemUserApi.SystemUser[]>([]);

// 计算属性
const filteredList = computed(() => {
  return userList.value.filter(item => item.status === 'active');
});

// 方法
function handleClick() {
  // ...
}

// 生命周期
onMounted(() => {
  // ...
});
</script>

<template>
  <!-- 模板内容 -->
</template>

<style scoped lang="scss">
/* 样式 */
</style>
```

### 导入顺序

按以下顺序组织导入语句：

1. **类型导入**（`import type`）
2. **Vue 核心**（vue、vue-router）
3. **第三方库**（dayjs、ant-design-vue 等）
4. **项目包**（@vben/* 开头）
5. **本地模块**（#/ 开头）
6. **相对路径导入**（./、../ 开头）

```typescript
// ✅ 正确的导入顺序
import type { Recordable } from '@vben/types';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

import dayjs from 'dayjs';

import { Page, useVbenModal } from '@vben/common-ui';
import { useAccess } from '@vben/access';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getUserList } from '#/api';
import { $t } from '#/locales';

import { useColumns } from './data';
import UserModal from './modules/UserModal.vue';
```

## 组件命名

### 文件命名

- **页面组件**：`index.vue`（放在功能目录下）
- **子组件**：使用 PascalCase，如 `UserModal.vue`、`UserTransfer.vue`
- **数据文件**：`data.ts`（存放列配置、表单配置等）

```
views/
├── system/
│   └── user/
│       ├── index.vue          # 主页面
│       ├── data.ts            # 表格列配置、表单配置
│       └── modules/
│           ├── UserModal.vue  # 用户弹窗
│           └── Cascader.vue   # 级联选择器
```

### 组件引用

模板中使用 PascalCase：

```vue
<template>
  <Page auto-content-height>
    <Grid :table-title="$t('system.user.list')">
      <UserGroupCascader :row="row" />
    </Grid>
    <UserModal v-model:open="modalOpen" />
  </Page>
</template>
```

## 响应式数据

### Ref vs Reactive

- **简单类型**：使用 `ref`
- **对象/数组**：根据使用场景选择
  - 需要整体替换：使用 `ref`
  - 只修改属性：使用 `reactive`

```typescript
// ✅ 推荐
const loading = ref(false);
const count = ref(0);
const user = ref<User | null>(null);

// 查询参数（可能整体替换）
const queryParams = ref<Recordable<any>>({});

// 表单数据（只修改属性）
const formData = reactive({
  username: '',
  email: '',
});
```

### 类型标注

响应式数据必须标注类型：

```typescript
// ✅ 正确
const userList = ref<SystemUserApi.SystemUser[]>([]);
const currentUser = ref<SystemUserApi.SystemUser | null>(null);
const loading = ref<boolean>(false);

// ❌ 错误
const userList = ref([]);
const currentUser = ref(null);
```

## 计算属性

使用 `computed` 创建派生状态：

```typescript
// ✅ 简单计算
const fullName = computed(() => {
  return `${user.value.firstName} ${user.value.lastName}`;
});

// ✅ 带类型标注
const filteredList = computed<User[]>(() => {
  return userList.value.filter(item => item.status === 'active');
});

// ✅ 可写计算属性
const displayName = computed({
  get: () => user.value?.displayName || user.value?.username || '用户',
  set: (val) => {
    if (user.value) {
      user.value.displayName = val;
    }
  },
});
```

## 方法定义

### 命名规范

- **事件处理**：`handle` 前缀，如 `handleClick`、`handleSubmit`
- **数据获取**：`fetch` 或 `get` 前缀，如 `fetchUserList`、`getUserInfo`
- **初始化**：`init` 前缀，如 `initUserGroupTree`
- **操作动作**：动词开头，如 `openModal`、`closeModal`、`updateUser`

```typescript
// ✅ 事件处理
function handleClick() { }
function handleSubmit() { }
function handleCopySuccess() { }

// ✅ 数据获取
async function fetchUserList() { }
async function getUserInfo(id: string) { }

// ✅ 初始化
async function initUserGroupTree() { }
function initFormData() { }

// ✅ 操作动作
function openModal() { }
function closeModal() { }
async function updateUser(data: User) { }
```

### 异步方法

异步方法使用 `async/await`：

```typescript
// ✅ 推荐
async function fetchUserList() {
  loading.value = true;
  try {
    const res = await getUserList({
      pageNo: 1,
      pageSize: 10,
    });
    userList.value = res.list;
  } catch (error) {
    console.error('获取用户列表失败', error);
  } finally {
    loading.value = false;
  }
}

// ❌ 不推荐
function fetchUserList() {
  loading.value = true;
  getUserList({ pageNo: 1, pageSize: 10 })
    .then(res => {
      userList.value = res.list;
    })
    .catch(error => {
      console.error('获取用户列表失败', error);
    })
    .finally(() => {
      loading.value = false;
    });
}
```

## 生命周期

### 常用生命周期钩子

```typescript
import { onMounted, onBeforeUnmount, watch } from 'vue';

// 组件挂载后
onMounted(() => {
  initUserGroupTree();
  fetchUserList();
});

// 组件卸载前
onBeforeUnmount(() => {
  // 清理定时器、事件监听等
});

// 监听数据变化
watch(
  () => route.query.id,
  (newId) => {
    if (newId) {
      fetchUserDetail(newId as string);
    }
  },
  { immediate: true }
);
```

## 模板语法

### 指令使用

```vue
<template>
  <!-- v-if vs v-show -->
  <!-- 频繁切换用 v-show，条件渲染用 v-if -->
  <div v-if="userList.length > 0">有数据</div>
  <div v-show="loading">加载中...</div>

  <!-- v-for 必须有 key -->
  <div v-for="item in userList" :key="item.id">
    {{ item.name }}
  </div>

  <!-- 事件修饰符 -->
  <button @click.prevent="handleSubmit">提交</button>
  <input @keyup.enter="handleSearch" />

  <!-- 动态属性 -->
  <div :class="{ active: isActive, disabled: isDisabled }">
    {{ text }}
  </div>
  <div :style="{ color: textColor, fontSize: fontSize + 'px' }">
    {{ text }}
  </div>
</template>
```

### 插槽使用

```vue
<template>
  <!-- 具名插槽 -->
  <Grid :table-title="$t('system.user.list')">
    <template #toolbar-tools>
      <VxeTableExportButton :api="exportUserList" />
    </template>
    
    <template #userGroup="{ row }">
      <UserGroupCascader :row="row" />
    </template>
  </Grid>

  <!-- 默认插槽 -->
  <CopyUserModal class="w-[700px]">
    <div class="p-1">
      <UserTransfer :from-user-id="currentCopyRow?.id" />
    </div>
  </CopyUserModal>
</template>
```

## 组件通信

### Props

```typescript
// 子组件
interface Props {
  userId?: string;
  userInfo: User;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  userId: '',
  disabled: false,
});
```

### Emits

```typescript
// 子组件
interface Emits {
  (e: 'update:modelValue', value: string): void;
  (e: 'success'): void;
  (e: 'change', value: User): void;
}

const emit = defineEmits<Emits>();

function handleSuccess() {
  emit('success');
}
```

### 父子组件示例

```vue
<!-- 父组件 -->
<template>
  <UserModal
    v-model:open="modalOpen"
    :user-id="currentUserId"
    @success="handleModalSuccess"
  />
</template>

<script setup lang="ts">
const modalOpen = ref(false);
const currentUserId = ref('');

function handleModalSuccess() {
  modalOpen.value = false;
  fetchUserList();
}
</script>
```

```vue
<!-- 子组件 UserModal.vue -->
<script setup lang="ts">
interface Props {
  open: boolean;
  userId?: string;
}

interface Emits {
  (e: 'update:open', value: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

function handleClose() {
  emit('update:open', false);
}

async function handleSubmit() {
  // 提交逻辑
  emit('success');
  handleClose();
}
</script>
```

## 国际化

使用 `$t` 函数进行国际化：

```typescript
import { $t } from '#/locales';

// 在 script 中
const title = $t('system.user.list');
const message = $t('common.saveSuccess');

// 在模板中
```

```vue
<template>
  <div>{{ $t('system.user.title') }}</div>
  <Button>{{ $t('common.submit') }}</Button>
</template>
```

## 权限控制

使用 `v-access` 指令或 `hasAccessByCodes` 方法：

```vue
<script setup lang="ts">
import { useAccess } from '@vben/access';
import { PERMISSION_ENUM } from '#/enums';

const { hasAccessByCodes } = useAccess();

// 在逻辑中判断
const canExport = hasAccessByCodes([PERMISSION_ENUM.SystemUserExport]);
</script>

<template>
  <!-- 在模板中使用指令 -->
  <Button v-access="PERMISSION_ENUM.SystemUserCreate">
    {{ $t('common.create') }}
  </Button>
  
  <VxeTableExportButton
    v-access="PERMISSION_ENUM.SystemUserExport"
    :api="exportUserList"
  />
</template>
```

## 样式规范

### Scoped 样式

所有组件样式必须使用 `scoped`：

```vue
<style scoped lang="scss">
.home-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.intro-section {
  padding: 40px;
  
  .greeting-text {
    font-size: 28px;
    font-weight: 600;
    color: #262626;
  }
}
</style>
```

### Tailwind CSS

优先使用 Tailwind 工具类：

```vue
<template>
  <div class="flex items-center gap-4 p-4 rounded-md bg-card">
    <div class="w-[700px] p-1">
      <span class="text-primary font-semibold">{{ title }}</span>
    </div>
  </div>
</template>
```

### 响应式设计

使用媒体查询实现响应式：

```scss
.quick-nav-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .quick-nav-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .quick-nav-grid {
    gap: 16px;
  }
}
```

## 表格组件

### VXE Table 使用

项目使用 VXE Table 作为数据表格：

```typescript
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const res = await getUserList({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
          return {
            list: res.list,
            total: res.total,
          };
        },
      },
    },
  } as VxeTableGridOptions<User>,
});
```

### 列配置分离

将列配置提取到 `data.ts` 文件：

```typescript
// data.ts
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import { $t } from '#/locales';

export function useColumns(): VxeTableGridOptions<User>['columns'] {
  return [
    {
      field: 'username',
      title: $t('system.user.username'),
      width: 150,
      align: 'left',
      fixed: 'left',
    },
    {
      field: 'email',
      title: $t('system.user.email'),
      width: 200,
      align: 'left',
    },
    // ...更多列
  ];
}
```

## 模态框

使用 `useVbenModal` 创建模态框：

```typescript
import { useVbenModal } from '@vben/common-ui';

const [UserModal, userModalApi] = useVbenModal({
  title: '用户详情',
  footer: false,
  onCancel() {
    userModalApi.close();
  },
});

// 打开模态框
function openModal(user: User) {
  userModalApi.setData(user).open();
}

// 关闭模态框
function closeModal() {
  userModalApi.close();
}
```

```vue
<template>
  <UserModal class="w-[700px]">
    <div class="p-4">
      <!-- 模态框内容 -->
    </div>
  </UserModal>
</template>
```

## 性能优化

### 避免不必要的响应式

```typescript
// ✅ 常量不需要响应式
const TABS = [
  { key: 'all', label: '全部' },
  { key: 'active', label: '启用' },
];

// ✅ 配置对象不需要响应式
const tableConfig = {
  height: 'auto',
  keepSource: true,
};

// ❌ 不要把常量变成响应式
const tabs = ref([
  { key: 'all', label: '全部' },
]);
```

### 使用 v-once

对于不会改变的内容使用 `v-once`：

```vue
<template>
  <div v-once>
    <h1>{{ staticTitle }}</h1>
    <p>这段文字不会改变</p>
  </div>
</template>
```

### 懒加载组件

```typescript
import { defineAsyncComponent } from 'vue';

const UserModal = defineAsyncComponent(() => import('./modules/UserModal.vue'));
```

## 错误处理

### Try-Catch

异步操作使用 try-catch：

```typescript
async function fetchUserList() {
  loading.value = true;
  try {
    const res = await getUserList({ pageNo: 1, pageSize: 10 });
    userList.value = res.list;
  } catch (error) {
    console.error('获取用户列表失败', error);
    message.error('获取用户列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}
```

### 全局错误处理

API 错误已在 `request.ts` 中统一处理，组件中只需处理业务逻辑错误。

## 注释规范

### 函数注释

```typescript
/**
 * 获取用户列表
 * @param params 查询参数
 */
async function fetchUserList(params: QueryParams) {
  // ...
}

/**
 * 初始化用户组树
 * @param userId 用户ID（可选）
 */
async function initUserGroupTree(userId?: string) {
  // ...
}
```

### 复杂逻辑注释

```typescript
// 如果筛选器中没有年份，则使用当前年份作为默认值
const year = queryParams.value.year || String(dayjs().year());

// 将用户组ID转换为字符串数组，确保类型一致
item.groupIds = Array.isArray(item.groupIds)
  ? item.groupIds.map(String)
  : [];
```

## 常见问题

### 避免直接修改 Props

```typescript
// ❌ 错误：直接修改 props
function updateUser() {
  props.user.name = 'new name';
}

// ✅ 正确：通过 emit 通知父组件
function updateUser() {
  emit('update:user', { ...props.user, name: 'new name' });
}
```

### 避免在模板中使用复杂表达式

```vue
<!-- ❌ 错误 -->
<template>
  <div>{{ user.firstName + ' ' + user.lastName + ' (' + user.age + ')' }}</div>
</template>

<!-- ✅ 正确 -->
<script setup lang="ts">
const userDisplay = computed(() => {
  return `${user.value.firstName} ${user.value.lastName} (${user.value.age})`;
});
</script>

<template>
  <div>{{ userDisplay }}</div>
</template>
```

### 避免在 computed 中修改数据

```typescript
// ❌ 错误
const filteredList = computed(() => {
  userList.value.sort((a, b) => a.id - b.id); // 修改了原数组
  return userList.value.filter(item => item.status === 'active');
});

// ✅ 正确
const filteredList = computed(() => {
  return [...userList.value]
    .sort((a, b) => a.id - b.id)
    .filter(item => item.status === 'active');
});
```
