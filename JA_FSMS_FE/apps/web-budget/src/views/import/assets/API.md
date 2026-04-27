# 预算资产类型映射 - 接口说明（给前端）

**模块路径前缀：** `{baseUrl}/budget-asset-type-mapping`  
（`{baseUrl}` 为网关/应用根路径，如 `/budget-api`）

---

## 1. 分页查询

- **地址：** `POST /budget-asset-type-mapping/page`
- **说明：** 分页查询预算资产类型映射。`year`、`changeStatus` 精确匹配；其余 6 个字段支持模糊搜索。
- **请求体：** JSON，`BudgetAssetTypeMappingQueryParams`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |
| year | String | 否 | 年份（精确匹配） |
| changeStatus | String | 否 | 是否变更：UNCHANGED-不变，NEW-新增，MODIFY-修改（精确匹配） |
| budgetAssetTypeCode | String | 否 | 预算资产类型编码（模糊） |
| budgetAssetTypeName | String | 否 | 预算资产类型名称（模糊） |
| assetMajorCategoryCode | String | 否 | 资产大类编码（模糊） |
| assetMajorCategoryName | String | 否 | 资产大类名称（模糊） |
| erpAssetType | String | 否 | 资产类型编码/ERP资产类型（模糊） |
| assetTypeName | String | 否 | 资产类型名称（模糊） |

- **响应：** `CommonResult<PageResult<BudgetAssetTypeMappingPageVo>>`  
  列表项字段：id, budgetAssetTypeCode, budgetAssetTypeName, assetMajorCategoryCode, assetMajorCategoryName, erpAssetType, assetTypeName, year, changeStatus, creator, createTime, updater, updateTime

---

## 2. 导出 Excel

- **地址：** `POST /budget-asset-type-mapping/export`
- **说明：** 按与分页相同的条件导出全量数据为 Excel，支持与分页相同的筛选条件；不传 body 或传空对象则导出全部。
- **请求体：** JSON，与分页查询参数结构相同（`BudgetAssetTypeMappingQueryParams`），所有字段可选。
- **响应：** 文件流，`Content-Disposition: attachment; filename=预算资产类型映射导出.xlsx`，直接下载即可。

---

## 3. 根据主键获取单条（编辑回显）

- **地址：** `GET /budget-asset-type-mapping/get/{id}`
- **说明：** 根据主键获取一条，用于编辑回显（含同步字段与手工维护字段）。
- **路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 |

- **响应：** `CommonResult<BudgetAssetTypeMappingPageVo>`  
  字段同分页列表单条。

---

## 4. 保存手工维护字段（单条更新）

- **地址：** `POST /budget-asset-type-mapping/update`
- **说明：** 按主键更新手工维护字段，不修改 budgetAssetTypeCode、budgetAssetTypeName。
- **请求体：** JSON，`BudgetAssetTypeMappingUpdateReqVO`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 |
| assetMajorCategoryCode | String | 否 | 资产大类编码 |
| assetMajorCategoryName | String | 否 | 资产大类名称 |
| erpAssetType | String | 否 | 资产类型编码/ERP资产类型 |
| assetTypeName | String | 否 | 资产类型名称 |
| year | String | 否 | 年份 |
| changeStatus | String | 否 | 是否变更：UNCHANGED / NEW / MODIFY |

- **响应：** `CommonResult<Void>`

---

## 5. 从视图增量同步

- **地址：** `POST /budget-asset-type-mapping/sync`
- **说明：** 从视图 DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE 拉取 MEMBER_CD 以 CU205 开头的数据，已有（同 budget_asset_type_code + 当前年）不修改，仅新增；year 取当前年。
- **请求体：** 无（可传空 body `{}`）。
- **响应：** `CommonResult<String>`，内容为同步结果说明文案。

---

## 6. 批量导入 Excel

- **地址：** `POST /budget-asset-type-mapping/import`
- **说明：** 批量导入预算资产类型映射。请使用「导出」得到的 Excel 模板，表头一致（含主键列）。按「是否变更」列：不变-跳过，新增-插入，修改-按主键更新（修改时主键必填）。
- **请求体：** `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel 文件（.xlsx） |

- **响应：** `CommonResult<String>`，内容为导入结果说明（如：跳过/新增/修改/失败条数及失败原因）。

---

## 通用说明

- 所有接口均需按项目约定携带认证信息（如 Token）。
- 除导出为文件流外，其余接口响应均为 `CommonResult<T>` 结构（code、data、msg 等按项目统一格式）。
