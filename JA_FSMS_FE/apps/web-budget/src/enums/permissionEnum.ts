export enum PERMISSION_ENUM {
  // 系统
  CommonPreferences = 'Common:Preferences', // 偏好设置
  // 导入配置

  ImportAssetsExport = 'Import:Assets:Export', // 资产配置-导出
  ImportAssetsImport = 'Import:Assets:Import', // 资产配置-导入
  ImportOrgsApiSync = 'Import:Orgs:ApiSync', // 组织配置-接口数据更新
  ImportOrgsExport = 'Import:Orgs:Export', // 组织配置-导出
  ImportOrgsImport = 'Import:Orgs:Import', // 组织配置-导入
  ImportProjectsApiSync = 'Import:Projects:ApiSync', // 组织配置-接口数据更新
  ImportProjectsExport = 'Import:Projects:Export', // 项目配置-导出
  ImportProjectsImport = 'Import:Projects:Import', // 项目配置-导入
  ImportSubjectsApiSync = 'Import:Subjects:ApiSync', // 组织配置-接口数据更新
  ImportSubjectsExport = 'Import:Subjects:Export', // 科目配置-导出
  ImportSubjectsImport = 'Import:Subjects:Import', // 科目配置-导入
  // 菜单管理
  SystemMenuI18NCONFIG = 'System:Menu:i18nConfig', // 多语言配置
  // 组织管理
  SystemOrgBatchAuth = 'System:Org:BatchAuth', // 批量授权
  SystemOrgExport = 'System:Org:Export', // 导出
  SystemOrgImport = 'System:Org:Import', // 导入
  SystemProjectBatchAuth = 'System:Project:BatchAuth', // 批量授权
  // 项目管理
  SystemProjectExport = 'System:Project:Export', // 导出
  SystemProjectImport = 'System:Project:Import', // 导入
  // 用户管理
  SystemUserExport = 'System:User:Export', // 导出
  // 用户组管理
  SystemUserGroupExport = 'System:UserGroup:Export', // 导出
  SystemUserGroupImport = 'System:UserGroup:Import', // 导入
  SystemUserImport = 'System:User:Import', // 导入
  SystemUserResetPassword = 'System:User:ResetPassword', // 重置密码
}
