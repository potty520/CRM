# CRM客户关系管理系统

## 软件需求规格说明书（SRS）

------

# 1. 项目概述

## 1.1 项目名称

CRM客户关系管理系统（Customer Relationship Management System）

------

## 1.2 项目背景

随着企业客户数量增加，传统Excel或手工管理方式已无法满足销售、客户跟进、商机管理、合同管理等业务需求。
本系统旨在建设一套统一的CRM客户管理平台，实现：

- 客户信息集中化
- 销售过程数字化
- 商机全流程跟踪
- 数据分析可视化
- 权限精细化控制

提升企业销售效率与客户转化率。

------

## 1.3 项目目标

建设企业级CRM系统，实现：

- 客户全生命周期管理
- 销售流程标准化
- 数据统一管理
- 提升团队协同效率
- 提供经营数据分析能力

------

# 2. 技术架构

## 2.1 技术选型

### 后端技术

| 技术            | 版本       |
| --------------- | ---------- |
| JDK             | 1.8        |
| Spring Boot     | 2.7.x      |
| Spring MVC      | 5.x        |
| Spring Security | 5.x        |
| MyBatis Plus    | 3.5.x      |
| MySQL           | 8.x        |
| Redis           | 6.x        |
| Maven           | 3.8+       |
| JWT             | 最新稳定版 |
| Swagger         | 3.x        |

------

### 前端技术

| 技术         | 版本        |
| ------------ | ----------- |
| Vue          | Vue2 / Vue3 |
| Element UI   | 2.x         |
| Axios        | 最新稳定版  |
| Vue Router   | 4.x         |
| Vuex / Pinia | 最新版      |
| ECharts      | 5.x         |
| Node.js      | 16+         |

------

## 2.2 系统架构

采用前后端分离架构：

```text
Vue前端
   ↓
Nginx
   ↓
Spring Boot API
   ↓
MySQL + Redis
```

------

# 3. 功能模块设计

------

# 3.1 用户与权限模块

## 功能描述

实现系统用户、角色、菜单、权限管理。

------

## 功能列表

| 功能     | 描述                 |
| -------- | -------------------- |
| 用户管理 | 用户新增、编辑、禁用 |
| 角色管理 | 销售、经理、管理员   |
| 菜单权限 | 动态菜单控制         |
| 数据权限 | 部门数据隔离         |
| 登录认证 | JWT Token认证        |
| 操作日志 | 用户行为记录         |

------

## 权限模型

采用RBAC权限模型：

```text
用户(User)
  ↓
角色(Role)
  ↓
权限(Permission)
```

------

# 3.2 客户管理模块

## 功能描述

管理客户基础信息及跟进状态。

------

## 功能列表

| 功能     | 描述         |
| -------- | ------------ |
| 客户新增 | 创建客户档案 |
| 客户编辑 | 修改客户信息 |
| 客户删除 | 逻辑删除     |
| 客户查询 | 多条件筛选   |
| 客户导入 | Excel导入    |
| 客户导出 | Excel导出    |
| 客户分配 | 分配销售人员 |
| 公海客户 | 自动回收机制 |

------

## 客户字段设计

| 字段     | 类型         |
| -------- | ------------ |
| 客户名称 | varchar(200) |
| 联系人   | varchar(100) |
| 手机号   | varchar(20)  |
| 邮箱     | varchar(100) |
| 地址     | varchar(500) |
| 客户等级 | int          |
| 客户来源 | varchar(50)  |
| 所属销售 | bigint       |
| 创建时间 | datetime     |

------

# 3.3 联系人管理模块

## 功能描述

管理客户联系人信息。

------

## 功能列表

| 功能       | 描述       |
| ---------- | ---------- |
| 联系人新增 | 创建联系人 |
| 联系人编辑 | 修改联系人 |
| 联系人删除 | 删除联系人 |
| 联系人查询 | 多条件查询 |
| 联系记录   | 沟通历史   |

------

# 3.4 商机管理模块

## 功能描述

管理销售商机及推进阶段。

------

## 商机阶段

```text
初步接触
↓
需求确认
↓
方案报价
↓
商务谈判
↓
合同签订
↓
成交
```

------

## 功能列表

| 功能       | 描述         |
| ---------- | ------------ |
| 商机创建   | 创建销售机会 |
| 商机跟进   | 更新阶段     |
| 金额预测   | 预估成交金额 |
| 成交概率   | 销售预测     |
| 商机转合同 | 自动生成合同 |

------

# 3.5 跟进记录模块

## 功能描述

记录销售人员客户跟进情况。

------

## 功能列表

| 功能     | 描述          |
| -------- | ------------- |
| 电话记录 | 电话沟通      |
| 拜访记录 | 上门拜访      |
| 微信记录 | 在线沟通      |
| 跟进提醒 | 下次跟进时间  |
| 附件上传 | 上传图片/文件 |

------

# 3.6 合同管理模块

## 功能描述

管理销售合同及回款。

------

## 功能列表

| 功能     | 描述     |
| -------- | -------- |
| 合同新增 | 创建合同 |
| 合同审批 | 多级审批 |
| 回款管理 | 回款登记 |
| 发票管理 | 开票记录 |
| 合同附件 | PDF上传  |

------

# 3.7 数据统计模块

## 功能描述

销售数据统计与分析。

------

## 图表内容

| 图表     | 描述       |
| -------- | ---------- |
| 销售漏斗 | 商机转化率 |
| 销售排行 | 销售业绩   |
| 客户增长 | 客户趋势   |
| 回款统计 | 财务统计   |
| 合同金额 | 月度分析   |

------

# 4. 非功能需求

------

# 4.1 性能需求

| 项目         | 指标       |
| ------------ | ---------- |
| 登录响应时间 | ≤2秒       |
| 查询响应时间 | ≤3秒       |
| 并发用户     | 1000+      |
| 数据量       | 百万级客户 |

------

# 4.2 安全需求

| 功能      | 描述          |
| --------- | ------------- |
| JWT认证   | Token校验     |
| 密码加密  | BCrypt        |
| SQL防注入 | MyBatis预编译 |
| XSS防御   | 输入过滤      |
| HTTPS     | 数据加密      |

------

# 4.3 可用性需求

- 支持Chrome、Edge浏览器
- 支持1920×1080分辨率
- 支持移动端适配

------

# 5. 数据库设计

------

# 5.1 用户表（sys_user）

```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100),
    password VARCHAR(200),
    real_name VARCHAR(100),
    phone VARCHAR(20),
    status TINYINT,
    create_time DATETIME
);
```

------

# 5.2 客户表（crm_customer）

```sql
CREATE TABLE crm_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(200),
    contact_name VARCHAR(100),
    mobile VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    level INT,
    source VARCHAR(50),
    owner_id BIGINT,
    create_time DATETIME
);
```

------

# 5.3 商机表（crm_business）

```sql
CREATE TABLE crm_business (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_name VARCHAR(200),
    customer_id BIGINT,
    amount DECIMAL(18,2),
    stage VARCHAR(50),
    owner_id BIGINT,
    create_time DATETIME
);
```

------

# 5.4 跟进记录表（crm_follow）

```sql
CREATE TABLE crm_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT,
    content TEXT,
    follow_type VARCHAR(50),
    next_follow_time DATETIME,
    create_by BIGINT,
    create_time DATETIME
);
```

------

# 6. 接口规范

------

# 6.1 登录接口

## 请求地址

```http
POST /api/auth/login
```

------

## 请求参数

```json
{
  "username": "admin",
  "password": "123456"
}
```

------

## 返回结果

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "jwt-token"
  }
}
```

------

# 6.2 客户列表接口

## 请求地址

```http
GET /api/customer/page
```

------

## 请求参数

| 参数         | 类型   |
| ------------ | ------ |
| pageNum      | int    |
| pageSize     | int    |
| customerName | string |

------

# 7. 前端页面设计

------

# 7.1 页面结构

```text
登录页
首页Dashboard
├── 客户管理
├── 联系人管理
├── 商机管理
├── 合同管理
├── 数据统计
└── 系统管理
```

------

# 7.2 Dashboard内容

- 今日新增客户
- 本月成交金额
- 销售排行榜
- 待跟进客户
- 销售漏斗图

------

# 8. 部署方案

------

# 8.1 开发环境

| 软件    | 版本 |
| ------- | ---- |
| JDK     | 1.8  |
| MySQL   | 8.0  |
| Redis   | 6.x  |
| Node.js | 16+  |

------

# 8.2 部署架构

```text
Nginx
  ↓
Vue静态资源

Spring Boot Jar
  ↓
MySQL
Redis
```

------

# 8.3 Linux启动命令

```bash
java -jar crm-system.jar
```

------

# 9. 项目目录结构

------

# 9.1 后端结构

```text
crm-server
├── controller
├── service
├── mapper
├── entity
├── dto
├── vo
├── config
├── security
└── common
```

------

# 9.2 前端结构

```text
crm-web
├── api
├── views
├── components
├── router
├── store
├── utils
└── assets
```

------

# 10. 开发计划

| 阶段       | 时间 |
| ---------- | ---- |
| 需求分析   | 1周  |
| 数据库设计 | 3天  |
| 后端开发   | 3周  |
| 前端开发   | 3周  |
| 联调测试   | 1周  |
| 上线部署   | 2天  |

------

# 11. 风险评估

| 风险       | 解决方案      |
| ---------- | ------------- |
| 数据量过大 | 分页+索引优化 |
| 权限复杂   | RBAC模型      |
| 并发访问   | Redis缓存     |
| 数据安全   | HTTPS+JWT     |

------

# 12. 后续扩展方向

- OA审批集成
- 微信企业号集成
- AI客户分析
- BI数据大屏
- 移动端APP
- 呼叫中心集成
- ERP系统对接