-- ============================================================
-- RBAC 建表脚本（sys_user / sys_role / sys_user_role /
--                sys_permission / sys_role_permission / sys_department）
-- 在 group_buy_market 库执行（或你的目标库）
-- 字段与 mapper xml 的 Base_Column_List 完全对齐
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username      VARCHAR(64)           DEFAULT NULL  COMMENT '展示名称',
  account       VARCHAR(64)  NOT NULL              COMMENT '登录账号，唯一',
  password      VARCHAR(128) NOT NULL              COMMENT 'BCrypt 加密密码',
  department_id BIGINT                DEFAULT NULL  COMMENT '所属部门ID',
  status        TINYINT               DEFAULT 1     COMMENT '1启用，0禁用',
  is_deleted    TINYINT               DEFAULT 0     COMMENT '0未删除，1已删除',
  createtime    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updatetime    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
  id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  org_id     BIGINT               DEFAULT 1     COMMENT '组织/租户ID，单组织默认1',
  role_code  VARCHAR(64) NOT NULL              COMMENT '角色编码，唯一，如 admin',
  role_name  VARCHAR(64)          DEFAULT NULL  COMMENT '角色名称',
  data_scope VARCHAR(32)          DEFAULT 'self' COMMENT 'all/dept/dept_and_below/self',
  sort_order INT                  DEFAULT 0     COMMENT '排序，越小越靠前',
  status     TINYINT              DEFAULT 1     COMMENT '1启用，0禁用',
  remark     VARCHAR(255)         DEFAULT NULL  COMMENT '备注',
  is_deleted TINYINT              DEFAULT 0     COMMENT '0未删除，1已删除',
  createtime DATETIME             DEFAULT CURRENT_TIMESTAMP,
  updatetime DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
  id         BIGINT NOT NULL AUTO_INCREMENT,
  user_id    BIGINT NOT NULL COMMENT '用户ID',
  role_id    BIGINT NOT NULL COMMENT '角色ID',
  createtime DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
  id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  parent_id  BIGINT                DEFAULT 0     COMMENT '上级权限ID，0表示根节点',
  perm_code  VARCHAR(128) NOT NULL              COMMENT '权限编码，唯一，如 okr:objective:create',
  perm_name  VARCHAR(64)           DEFAULT NULL  COMMENT '权限名称',
  perm_type  VARCHAR(16)           DEFAULT NULL  COMMENT 'menu菜单/button按钮/api接口',
  path       VARCHAR(255)          DEFAULT NULL  COMMENT '前端路由或接口路径',
  sort_order INT                  DEFAULT 0     COMMENT '排序',
  status     TINYINT              DEFAULT 1     COMMENT '1启用，0禁用',
  remark     VARCHAR(255)          DEFAULT NULL  COMMENT '备注',
  is_deleted TINYINT              DEFAULT 0     COMMENT '0未删除，1已删除',
  createtime DATETIME             DEFAULT CURRENT_TIMESTAMP,
  updatetime DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  role_id       BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  createtime    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updatetime    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与权限关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_department (
  id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  org_id         BIGINT               DEFAULT 1     COMMENT '组织/租户ID',
  parent_id      BIGINT               DEFAULT 0     COMMENT '上级部门ID，0表示根部门',
  dept_name      VARCHAR(64)          DEFAULT NULL  COMMENT '部门名称',
  dept_code      VARCHAR(64)          DEFAULT NULL  COMMENT '部门编码',
  leader_user_id BIGINT               DEFAULT NULL  COMMENT '部门负责人用户ID',
  sort_order     INT                  DEFAULT 0     COMMENT '排序',
  status         TINYINT              DEFAULT 1     COMMENT '1启用，0禁用',
  remark         VARCHAR(255)         DEFAULT NULL  COMMENT '备注',
  is_deleted     TINYINT              DEFAULT 0     COMMENT '0未删除，1已删除',
  createtime     DATETIME             DEFAULT CURRENT_TIMESTAMP,
  updatetime     DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
