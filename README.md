# CRM客户关系管理系统

基于需求文档实现的完整前后端分离 CRM 系统。

## 技术栈

- **后端**: Spring Boot 2.7 + Spring Security + JWT + MyBatis Plus + MySQL
- **前端**: Vue 3 + Element Plus + Pinia + Vue Router + ECharts + Vite

## 功能模块

| 模块 | 功能 |
|------|------|
| 用户与权限 | JWT登录、用户/角色管理、RBAC权限、操作日志 |
| 客户管理 | 增删改查、导入导出、分配销售、公海客户 |
| 联系人 | 联系人CRUD、关联客户 |
| 商机管理 | 六阶段推进、成交概率、商机转合同 |
| 跟进记录 | 电话/拜访/微信记录、跟进提醒 |
| 合同管理 | 合同审批、回款登记、发票记录 |
| 数据统计 | 销售漏斗、排行榜、客户趋势、回款统计 |

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < sql/schema.sql
```

修改 `crm-server/src/main/resources/application.yml` 中的数据库账号密码。

### 2. 启动后端

```bash
cd crm-server
mvn spring-boot:run
```

后端地址: http://localhost:8080  
Swagger文档: http://localhost:8080/swagger-ui.html

### 3. 启动前端

```bash
cd crm-web
npm install
npm run dev
```

前端地址: http://localhost:3000

### 4. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| sales1 | 123456 | 销售员 |
| sales2 | 123456 | 销售员 |

## 项目结构

```
CRM/
├── sql/schema.sql          # 数据库脚本
├── crm-server/             # Spring Boot 后端
└── crm-web/                # Vue3 前端
```

## API 示例

- 登录: `POST /api/auth/login`
- 客户列表: `GET /api/customer/page?pageNum=1&pageSize=10`
- 仪表盘: `GET /api/dashboard/overview`
