# CRM系统功能测试报告

**项目**：CRM客户关系管理系统  
**测试时间**：2026-05-26  
**测试环境**：后端 `http://localhost:8080` / 前端 `http://localhost:3001`  
**测试账号**：admin / 123456（管理员）

---

## 一、测试用例汇总

### 1. 登录模块

| 用例ID | 模块 | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|------|--------|------|------|----------|----------|
| L001 | 登录 | 正常登录 | POST `/api/auth/login` | `{"username":"admin","password":"123456"}` | 返回token，code=200 | ✅ 通过 |
| L002 | 登录 | 用户信息 | GET `/api/auth/info` | Header: Authorization Bearer Token | 返回用户信息和菜单 | ✅ 通过 |

### 2. 客户管理模块

| 用例ID | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|--------|------|------|----------|----------|
| C001 | 客户列表 | GET `/api/customer/page` | `pageNum=1&pageSize=5` | 分页返回客户列表 | ✅ 通过 |
| C002 | 新增客户 | POST `/api/customer` | 客户信息JSON | 新增成功code=200 | ✅ 通过 |
| C003 | 客户分配 | PUT `/api/customer/assign` | `customerId&ownerId` | 分配成功 | ✅ 通过 |
| C004 | 放入公海 | PUT `/api/customer/pool/{id}` | 客户ID | poolStatus=1 | ✅ 通过 |
| C005 | 领取公海客户 | PUT `/api/customer/claim/{id}` | 客户ID | poolStatus=0,ownerId当前用户 | ✅ 通过 |
| C006 | 客户导入 | POST `/api/customer/import` | MultipartFile | 成功导入N条 | ✅ 通过 |
| C007 | 客户导出 | GET `/api/customer/export` | 可选customerName参数 | 下载Excel文件 | ✅ 通过 |

### 3. 合同管理模块

| 用例ID | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|--------|------|------|----------|----------|
| CT001 | 合同列表 | GET `/api/contract/page` | `pageNum=1&pageSize=10` | 分页返回合同列表 | ✅ 通过 |
| CT002 | 新增合同 | POST `/api/contract` | 合同信息JSON | 新增成功code=200 | ✅ 通过 |
| CT003 | 提交审批 | PUT `/api/contract/approve/{id}` | `status=审批中` | 状态更新为审批中 | ✅ 通过（URL编码） |
| CT004 | 审批通过 | PUT `/api/contract/approve/{id}` | `status=已生效` | 状态更新为已生效 | ✅ 通过 |
| CT005 | 审批驳回 | PUT `/api/contract/approve/{id}` | `status=已驳回` | 状态更新为已驳回 | ✅ 通过 |
| CT006 | 回款登记 | POST `/api/contract/payment` | 回款信息JSON | 新增回款记录 | ✅ 通过 |
| CT007 | 回款记录 | GET `/api/contract/payment/{contractId}` | 合同ID | 返回该合同回款列表 | ✅ 通过 |
| CT008 | 发票登记 | POST `/api/contract/invoice` | 发票信息JSON | 新增发票记录 | ✅ 通过 |
| CT009 | 发票记录 | GET `/api/contract/invoice/{contractId}` | 合同ID | 返回该合同发票列表 | ✅ 通过 |
| CT010 | 合同导入 | POST `/api/contract/import` | MultipartFile | 成功导入N条 | ✅ 通过 |
| CT011 | 合同导出 | GET `/api/contract/export` | 可选contractName参数 | 下载Excel文件 | ✅ 通过 |

### 4. Dashboard模块

| 用例ID | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|--------|------|------|----------|----------|
| D001 | 概览数据 | GET `/api/dashboard/overview` | 无 | 返回客户总数、今日新增、商机数等 | ✅ 通过 |

---

## 二、API接口测试详情

### 2.1 认证接口

**POST /api/auth/login**
```json
// 请求
{"username": "admin", "password": "123456"}
// 响应
{"code": 200, "msg": "success", "data": {"token": "eyJhbG..."}}
```

**GET /api/auth/info**
```json
// 响应
{"code": 200, "msg": "success", "data": {
  "userId": 1,
  "username": "admin",
  "realName": "系统管理员",
  "roles": ["admin"],
  "menus": [
    {"menuName": "首页", "path": "/dashboard"},
    {"menuName": "客户管理", "path": "/customer"},
    {"menuName": "联系人", "path": "/contact"},
    {"menuName": "商机管理", "path": "/business"},
    {"menuName": "合同管理", "path": "/contract"},
    {"menuName": "数据统计", "path": "/statistics"},
    {"menuName": "系统管理", "path": "/system"}
  ]
}}
```

### 2.2 合同审批流测试

```
状态流转: 草稿 → 审批中 → 已生效
                      ↘ 已驳回
```

| 操作 | API | 状态变更 |
|------|-----|----------|
| 新增合同 | POST `/api/contract` | 状态默认为"草稿" |
| 提交审批 | PUT `/api/contract/approve/1?status=审批中` | 草稿→审批中 |
| 通过 | PUT `/api/contract/approve/1?status=已生效` | 审批中→已生效 |
| 驳回 | PUT `/api/contract/approve/1?status=已驳回` | 审批中→已驳回 |

### 2.3 回款/发票关联查询

```
合同ID=1 的回款记录:
[
  {"id": 1, "contractId": 1, "amount": 10000.0, "paymentDate": "2026-05-26", "createBy": 1}
]

合同ID=1 的发票记录:
[
  {"id": 1, "contractId": 1, "invoiceNo": "FP20260526001", "amount": 5000.0, "invoiceDate": "2026-05-26"}
]
```

---

## 三、前端功能截图说明

### 3.1 Dashboard首页
- 左侧菜单：首页、客户管理、联系人、商机管理、合同管理、数据统计、系统管理
- 顶部：系统管理员 / 退出按钮
- 内容区：今日新增客户(4)、客户总数(4)、商机总数(2)、本月成交金额(¥0)
- 图表：销售漏斗图、销售排行榜、待跟进客户列表

### 3.2 客户管理页面
- 搜索栏：客户名称输入框 / 客户类型下拉（私海/公海）
- 操作按钮：查询、新增、导出、导入
- 表格列：客户名称、联系人、手机号、等级（普通/重要/VIP）、来源、所属销售、类型（私海/公海）、操作
- 操作按钮：编辑、分配、放入公海/领取、删除

### 3.3 合同管理页面
- 搜索栏：合同名称输入框 / 状态筛选（草稿/审批中/已生效/已驳回）
- 操作按钮：查询、新增、导出、导入
- 表格列：合同编号、合同名称、客户、金额、状态、负责人、操作
- 操作按钮：编辑、提交审批、通过、驳回、回款、发票、删除

### 3.4 发票弹窗
- 表单字段：发票号（输入框）、发票金额（数字输入）、开票日期（日期选择）、备注（输入框）
- 历史记录表：发票号 | 金额 | 日期 | 备注
- 底部按钮：关闭、开票

---

## 四、已知问题

| 问题 | 描述 | 严重程度 | 备注 |
|------|------|----------|------|
| URL编码 | 中文status参数需URL编码才能正确传递 | 中 | 前端已处理，后端测试时需手动编码 |
| 状态显示 | "已驳回"显示为"已骗回"（文字错误） | 低 | 前端翻译问题，接口返回正确 |

---

## 五、测试结论

| 模块 | 用例数 | 通过 | 失败 | 通过率 |
|------|--------|------|------|--------|
| 登录 | 2 | 2 | 0 | 100% |
| 客户管理 | 7 | 7 | 0 | 100% |
| 合同管理 | 11 | 11 | 0 | 100% |
| Dashboard | 1 | 1 | 0 | 100% |
| **合计** | **21** | **21** | **0** | **100%** |

**结论**：所有功能测试通过，系统运行正常。

---

## 六、BI数据大屏模块

| 用例ID | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|--------|------|------|----------|----------|
| BI001 | 统计概览 | GET `/api/bi/stats` | 无 | 返回总客户/今日新增/本月新增/商机/合同金额等 | ✅ 通过 |
| BI002 | 月度趋势 | GET `/api/bi/trend` | `months=6` | 返回近N月客户+合同趋势 | ✅ 通过 |
| BI003 | 团队排行 | GET `/api/bi/team-ranking` | 无 | 返回各销售合同数/金额/商机数 | ✅ 通过 |
| BI004 | 漏斗数据 | GET `/api/bi/funnel` | 无 | 返回各阶段商机数量 | ✅ 通过 |
| BI005 | 客户来源 | GET `/api/bi/customer-source` | 无 | 返回来源分布 | ✅ 通过 |
| BI006 | 客户等级 | GET `/api/bi/customer-level` | 无 | 返回等级分布 | ✅ 通过 |
| BI007 | 商机转化率 | GET `/api/bi/business-winrate` | 无 | 返回成交数/转化率/总商机 | ✅ 通过 |

### 6.1 BI接口响应示例

**GET /api/bi/stats**
```json
{
  "code": 200, "msg": "success", "data": {
    "totalContractAmount": 0,
    "totalCustomers": 4,
    "todayNewCustomers": 4,
    "monthNewCustomers": 4,
    "monthContractAmount": 0,
    "totalBusiness": 2,
    "totalContracts": 1
  }
}
```

**GET /api/bi/funnel**
```json
{
  "code": 200, "msg": "success", "data": [
    {"stage": "初步接触", "count": 0},
    {"stage": "需求确认", "count": 0},
    {"stage": "方案报价", "count": 1},
    {"stage": "商务谈判", "count": 1},
    {"stage": "合同签订", "count": 0},
    {"stage": "成交", "count": 0}
  ]
}
```

---

## 七、OA审批集成模块

| 用例ID | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|--------|------|------|----------|----------|
| OA001 | 提交审批 | POST `/api/oa/submit` | flowName/steps等 | 创建审批流，状态pending | ✅ 通过 |
| OA002 | 待我审批 | GET `/api/oa/pending` | 无 | 返回待当前用户审批列表 | ✅ 通过 |
| OA003 | 我的申请 | GET `/api/oa/my-applications` | 无 | 返回我提交的审批列表 | ✅ 通过 |
| OA004 | 审批通过 | PUT `/api/oa/approve/{flowId}/{stepId}` | opinion参数 | 审批通过，状态更新 | ✅ 通过（表单格式） |
| OA005 | 审批驳回 | PUT `/api/oa/reject/{flowId}/{stepId}` | opinion参数 | 驳回审批 | ✅ 通过 |
| OA006 | 流程分页 | GET `/api/oa/flow/page` | pageNum/pageSize/status | 分页返回审批流列表 | ✅ 通过 |

### 7.1 OA审批状态流转

```
提交申请 → pending（待审批）
         ↓
    审批通过 → approved
    审批驳回 → rejected
```

### 7.2 OA接口响应示例

**POST /api/oa/submit**
```json
// 请求
{
  "flowName": "合同订单审批",
  "flowType": "contract",
  "bizId": 1,
  "bizType": "contract",
  "remark": "大额订单",
  "steps": [{"approverId": 1, "approverName": "系统管理员"}]
}
// 响应
{"code": 200, "msg": "success", "data": null}
```

**GET /api/oa/pending**
```json
{
  "code": 200, "msg": "success", "data": [
    {
      "stepNo": 1, "stepId": 2, "id": 2,
      "applicantName": "系统管理员",
      "applyTime": "2026-05-26T02:23:50",
      "flowName": "合同订单审批",
      "flowType": "contract",
      "status": "pending"
    }
  ]
}
```

---

## 八、ERP系统对接模块

| 用例ID | 功能点 | 请求 | 参数 | 预期结果 | 实际结果 |
|--------|--------|------|------|----------|----------|
| EP001 | 新增产品 | POST `/api/erp/product` | 产品信息JSON | 创建成功code=200 | ✅ 通过 |
| EP002 | 产品分页 | GET `/api/erp/product/page` | pageNum/pageSize/keyword | 分页返回产品列表 | ✅ 通过 |
| EP003 | 编辑产品 | PUT `/api/erp/product/{id}` | 产品信息JSON | 更新成功 | ✅ 通过 |
| EP004 | 删除产品 | DELETE `/api/erp/product/{id}` | 产品ID | 删除成功 | ✅ 通过 |
| EP005 | 产品导出 | GET `/api/erp/product/export` | 无 | 返回全部产品列表 | ✅ 通过 |
| EI001 | 新增库存 | POST `/api/erp/inventory` | 库存信息JSON | 创建成功 | ✅ 通过 |
| EI002 | 库存分页 | GET `/api/erp/inventory/page` | pageNum/pageSize/keyword | 分页返回库存 | ✅ 通过 |
| EI003 | 编辑库存 | PUT `/api/erp/inventory/{id}` | 库存信息JSON | 更新成功 | ✅ 通过 |
| EI004 | 删除库存 | DELETE `/api/erp/inventory/{id}` | 库存ID | 删除成功 | ✅ 通过 |
| EF001 | 新增财务 | POST `/api/erp/financial` | 财务信息JSON | 创建成功 | ✅ 通过 |
| EF002 | 财务分页 | GET `/api/erp/financial/page` | pageNum/pageSize/startDate/endDate/type | 分页返回财务记录 | ✅ 通过 |
| EF003 | 编辑财务 | PUT `/api/erp/financial/{id}` | 财务信息JSON | 更新成功 | ✅ 通过 |
| EF004 | 删除财务 | DELETE `/api/erp/financial/{id}` | 财务ID | 删除成功 | ✅ 通过 |

### 8.1 ERP接口响应示例

**POST /api/erp/product**
```json
// 请求
{
  "productCode": "P002",
  "productName": "笔记本电脑",
  "spec": "15.6寸",
  "category": "电子产品",
  "unit": "台",
  "price": 6999,
  "cost": 4500
}
// 响应
{"code": 200, "msg": "success", "data": null}
```

**GET /api/erp/product/page**
```json
{
  "code": 200, "msg": "success", "data": {
    "records": [
      {
        "id": 2,
        "productCode": "P002",
        "productName": "笔记本电脑",
        "spec": "15.6寸",
        "unit": "台",
        "price": 6999.00,
        "categoryId": null,
        "categoryName": "",
        "status": 1
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

**GET /api/erp/financial/page**
```json
{
  "code": 200, "msg": "success", "data": {
    "records": [
      {
        "id": 1,
        "billNo": "FIN002",
        "type": 1,
        "amount": 50000.00,
        "customerName": "测试公司",
        "recordDate": "2026-05-26",
        "remark": "首笔开票"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 九、测试结论（更新）

| 模块 | 用例数 | 通过 | 失败 | 通过率 |
|------|--------|------|------|--------|
| 登录 | 2 | 2 | 0 | 100% |
| 客户管理 | 7 | 7 | 0 | 100% |
| 合同管理 | 11 | 11 | 0 | 100% |
| Dashboard | 1 | 1 | 0 | 100% |
| BI数据大屏 | 7 | 7 | 0 | 100% |
| OA审批集成 | 6 | 6 | 0 | 100% |
| ERP系统对接 | 13 | 13 | 0 | 100% |
| **合计** | **47** | **47** | **0** | **100%** |

**结论**：全部47个测试用例通过，三大新功能（BI/ OA/ ERP）运行正常。