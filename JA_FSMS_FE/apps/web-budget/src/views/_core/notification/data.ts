import type { NotificationItem } from '@vben/layouts';

import { ref } from 'vue';

export const versionData = [
  {
    title: 'V.S251028.02',
    content: [
      'feat: 开放【对账报告-汇总】页面(提交/集团提交)功能',
      'fix: 修复#法人组织# 筛选框 无法多选bug',
    ],
  },
  {
    title: 'V.S251028.01',
    content: [
      'test: 提测【对账状态】页面',
      'feat: 统一#法人组织# 搜索项',
      'feat: 所有Table组件增加行悬停效果',
      'feat: 统一Table组件 工具栏',
      'fix: 修复【对账规则】保存bug',
    ],
  },
  {
    title: 'V.S251027.01',
    content: [
      'feat: 统一【对账管理】各页面的样式',
      'fix: 修复【对账规则】相关bug',
    ],
  },
  {
    title: 'V.S251024.01',
    content: [
      'test: 提测【对账报告-汇总】页面。不包含(提交/导出)功能',
      'test: 提测【对账报告-明细】页面。不包含(导出)功能',
      'test: 提测【差异原因填报-往来】页面',
      'test: 提测【差异原因填报-现流】页面',
      '注：以上页面均为功能提测，数据均假数据',
      'fix: 去掉所有table工具栏中的搜索区域隐藏/显示 按钮',
      'fix: 去掉【组织管理】和【表单配置】页面标题',
    ],
  },
  {
    title: 'V.S251021.01',
    content: [
      'feat: 开放【对账报告-汇总】页面(部分功能)。注：会计期间：2025-08',
      'feat: 开放【差异金额明细】页面(部分功能)',
      'feat: 开发【差异原因填报-往来】页面(部分功能)',
      'feat: 开发【差异原因填报-现流】页面(部分功能)',
    ],
  },
  {
    title: 'V.S251016.01',
    content: [
      'test: 提测【差异原因填报-往来】页面',
      'test: 提测【对账规则配置】页面',
      'feat: 增加【报表查看】演示页面',
      'feat: 增加【管理组织整体概览】演示页面',
      'feat: 增加【用户管理】页面用户组查看及搜索',
      'feat: 完成【对账规则配置】页面开发',
      'fix: 修复【填报】页面频率取值错误',
      'fix: 优化右上角头像显示(清理浏览器缓存生效)',
    ],
  },
  {
    title: 'V.S251011.01',
    content: [
      'feat: 增加【偏好配置】权限控制',
      'feat: 增加【主题切换】权限控制',
      'feat: 增加【语言切换】权限控制',
      'feat: 增加【全屏】权限控制',
      'feat: 初步实现【通知】功能，目前仅用于版本更新通知',
      'feat: 完成【对账规则配置】页面开发',
      'fix: 修复【对账规则配置->对账科目】子科目参数错误',
      'fix: 修复【登录】密码输入框重影',
      'fix: 修复弹窗z-index问题',
      'fix: 【角色组管理】，添加角色组名称筛选',
    ],
  },
];

export const notifications = ref<NotificationItem[]>([
  {
    avatar: 'https://avatar.vercel.sh/satori',
    date: '刚刚',
    isRead: false,
    message: `版本号：${versionData[0]?.title}`,
    title: '检测到版本更新',
  },
]);
