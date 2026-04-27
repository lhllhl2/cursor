# 表格页面生成规范

## 配置文件说明

当用户提供 `.kiro/specs/tables/*.json` 配置文件时，按照以下规范生成表格页面代码。

## 配置文件结构

```json
{
  "module": "report",           // 模块名（对应 views 下的目录）
  "name": "payment",            // 页面名称（对应具体功能目录）
  "title": "付款单列表",         // 页面标题
  "api": {
    "baseUrl": "/report-api/payment",  // API 基础路径
    "list": "/list",            // 列表接口（可选，默认 /list）
    "detail": "/detail",        // 详情接口（可选，默认 /detail）
    "create": "/create",        // 创建接口（可选，默认 /create）
    "update": "/update",        // 更新接口（可选，默认 /update）
    "delete": "/delete",        // 删除接口（可选，默认 /delete）
    "export": "/export"         // 导出接口（可选，默认 /export）
  },
  "permissions": {              // 权限配置（可选）
    "view": "report:payment:view",
    "create": "report:payment:create",
    "update": "report:payment:update",
    "delete": "report:payment:delete",
    "export": "report:payment:export"
  },
  "columns": [                  // 列配置
    {
      "field": "username",      // 字段名
      "title": "用户名",         // 列标题
      "width": 150,             // 列宽度（可选）
      "align": "left",          // 对齐方式：left/center/right（可选，默认 left）
      "fixed": "left",          // 固定列：left/right（可选）
      "sortable": true,         // 是否可排序（可选，默认 false）
      "filterable": true,       // 是否可筛选（可选，默认 false）
      "filterType": "input",    // 筛选类型：input/select/date/daterange（可选）
      "editable": false,        // 是否可编辑（可选，默认 false）
      "editType": "input",      // 编辑类型：input/select/date/number（可选）
      "required": false,        // 是否必填（可选，默认 false）
      "dict": "",               // 字典类型（可选，用于 select）
      "format": "",             // 格式化函数（可选，如 date）
      "slot": false             // 是否使用插槽（可选，默认 false）
    }
  ],
  "features": {                 // 功能配置（可选）
    "create": true,             // 是否支持新增
    "update": true,             // 是否支持编辑
    "delete": true,             // 是否支持删除
    "export": true,             // 是否支持导出
    "import": false,            // 是否支持导入
    "batchDelete": true         // 是否支持批量删除
  }
}
