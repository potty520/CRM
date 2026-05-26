-- CRM 客户关系管理系统数据库
CREATE DATABASE IF NOT EXISTS crm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE crm_db;

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(100),
    phone VARCHAR(20),
    dept_id BIGINT,
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    remark VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(100) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    perms VARCHAR(100),
    menu_type CHAR(1) COMMENT 'M目录 C菜单 F按钮',
    icon VARCHAR(50),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 操作日志
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(100),
    module VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 客户表
CREATE TABLE IF NOT EXISTS crm_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(200) NOT NULL,
    contact_name VARCHAR(100),
    mobile VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    level INT DEFAULT 1 COMMENT '1普通 2重要 3VIP',
    source VARCHAR(50),
    owner_id BIGINT,
    dept_id BIGINT,
    pool_status TINYINT DEFAULT 0 COMMENT '0私海 1公海',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 联系人表
CREATE TABLE IF NOT EXISTS crm_contact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(20),
    email VARCHAR(100),
    position VARCHAR(100),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商机表
CREATE TABLE IF NOT EXISTS crm_business (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_name VARCHAR(200) NOT NULL,
    customer_id BIGINT NOT NULL,
    amount DECIMAL(18,2) DEFAULT 0,
    stage VARCHAR(50) DEFAULT '初步接触',
    probability INT DEFAULT 10 COMMENT '成交概率%',
    owner_id BIGINT,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 跟进记录表
CREATE TABLE IF NOT EXISTS crm_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    business_id BIGINT,
    content TEXT,
    follow_type VARCHAR(50) COMMENT '电话/拜访/微信',
    next_follow_time DATETIME,
    attachment VARCHAR(500),
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 合同表
CREATE TABLE IF NOT EXISTS crm_contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_no VARCHAR(100) NOT NULL,
    contract_name VARCHAR(200) NOT NULL,
    customer_id BIGINT NOT NULL,
    business_id BIGINT,
    amount DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(50) DEFAULT '草稿' COMMENT '草稿/审批中/已生效/已驳回',
    owner_id BIGINT,
    sign_date DATE,
    attachment VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 回款表
CREATE TABLE IF NOT EXISTS crm_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    payment_date DATE,
    remark VARCHAR(200),
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 发票表
CREATE TABLE IF NOT EXISTS crm_invoice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id BIGINT NOT NULL,
    invoice_no VARCHAR(100),
    amount DECIMAL(18,2),
    invoice_date DATE,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 初始化数据
INSERT INTO sys_dept (dept_name, parent_id) VALUES ('总公司', 0), ('销售一部', 1), ('销售二部', 1);

INSERT INTO sys_role (role_name, role_code, remark) VALUES
('管理员', 'admin', '系统管理员'),
('销售经理', 'manager', '销售经理'),
('销售员', 'sales', '普通销售');

INSERT INTO sys_user (username, password, real_name, phone, dept_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 1, 1),
('sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '13800000001', 2, 1),
('sales2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '13800000002', 3, 1);
-- 默认密码均为 123456

INSERT INTO sys_user_role VALUES (1, 1), (2, 3), (3, 3);

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort) VALUES
(0, '首页', '/dashboard', 'dashboard/index', 'dashboard:view', 'C', 'Odometer', 1),
(0, '客户管理', '/customer', 'customer/index', 'customer:list', 'C', 'User', 2),
(0, '联系人', '/contact', 'contact/index', 'contact:list', 'C', 'Phone', 3),
(0, '商机管理', '/business', 'business/index', 'business:list', 'C', 'TrendCharts', 4),
(0, '合同管理', '/contract', 'contract/index', 'contract:list', 'C', 'Document', 5),
(0, '数据统计', '/statistics', 'statistics/index', 'statistics:view', 'C', 'DataAnalysis', 6),
(0, '系统管理', '/system', NULL, NULL, 'M', 'Setting', 7),
(7, '用户管理', '/system/user', 'system/user', 'system:user', 'C', 'UserFilled', 1),
(7, '角色管理', '/system/role', 'system/role', 'system:role', 'C', 'Avatar', 2),
(7, '操作日志', '/system/log', 'system/log', 'system:log', 'C', 'List', 3);

INSERT INTO sys_role_menu SELECT 1, id FROM sys_menu;

INSERT INTO crm_customer (customer_name, contact_name, mobile, email, level, source, owner_id, dept_id) VALUES
('华为技术有限公司', '王经理', '13900001111', 'wang@huawei.com', 3, '官网', 2, 2),
('阿里巴巴集团', '李总监', '13900002222', 'li@alibaba.com', 3, '展会', 2, 2),
('腾讯科技', '张主管', '13900003333', 'zhang@tencent.com', 2, '转介绍', 3, 3);

INSERT INTO crm_contact (customer_id, contact_name, mobile, position) VALUES
(1, '王经理', '13900001111', '采购经理'),
(2, '李总监', '13900002222', '技术总监');

INSERT INTO crm_business (business_name, customer_id, amount, stage, probability, owner_id) VALUES
('华为云服务采购', 1, 500000.00, '方案报价', 60, 2),
('阿里数据中台', 2, 800000.00, '商务谈判', 75, 2);

INSERT INTO crm_follow (customer_id, content, follow_type, next_follow_time, create_by) VALUES
(1, '电话沟通需求，客户有意向', '电话', DATE_ADD(NOW(), INTERVAL 3 DAY), 2),
(2, '上门拜访演示方案', '拜访', DATE_ADD(NOW(), INTERVAL 7 DAY), 2);
