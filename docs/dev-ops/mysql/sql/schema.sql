DROP TABLE IF EXISTS okr_operation_log;
DROP TABLE IF EXISTS okr_check_in_item;
DROP TABLE IF EXISTS okr_check_in;
DROP TABLE IF EXISTS okr_progress_record;
DROP TABLE IF EXISTS okr_objective_alignment;
DROP TABLE IF EXISTS okr_cycle;
DROP TABLE IF EXISTS okr_task_user;
DROP TABLE IF EXISTS okr_objective_user;
DROP TABLE IF EXISTS okr_task;
DROP TABLE IF EXISTS okr_key_result;
DROP TABLE IF EXISTS okr_objective;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_department;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '展示名称',
    account VARCHAR(50) NOT NULL COMMENT '登录账号，唯一',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    department_id BIGINT NULL COMMENT '所属部门ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1启用，0禁用',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_account (account),
    KEY idx_sys_user_department_id (department_id),
    KEY idx_sys_user_status (status),
    KEY idx_sys_user_createtime (createtime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    org_id BIGINT NOT NULL DEFAULT 1 COMMENT '组织/租户ID，单组织系统默认1',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级部门ID，0表示根部门',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NULL COMMENT '部门编码',
    leader_user_id BIGINT NULL COMMENT '部门负责人用户ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_department_org_parent_name_deleted (org_id, parent_id, dept_name, is_deleted),
    UNIQUE KEY uk_sys_department_org_code_deleted (org_id, dept_code, is_deleted),
    KEY idx_sys_department_parent (org_id, parent_id, is_deleted),
    KEY idx_sys_department_status (org_id, status, is_deleted),
    KEY idx_sys_department_leader (leader_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

SET @sys_user_department_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND column_name = 'department_id'
);
SET @sys_user_department_id_sql := IF(
    @sys_user_department_id_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN department_id BIGINT NULL COMMENT ''所属部门ID'' AFTER role',
    'SELECT 1'
);
PREPARE sys_user_department_id_stmt FROM @sys_user_department_id_sql;
EXECUTE sys_user_department_id_stmt;
DEALLOCATE PREPARE sys_user_department_id_stmt;

SET @idx_sys_user_department_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_sys_user_department_id'
);
SET @idx_sys_user_department_id_sql := IF(
    @idx_sys_user_department_id_exists = 0,
    'CREATE INDEX idx_sys_user_department_id ON sys_user(department_id)',
    'SELECT 1'
);
PREPARE idx_sys_user_department_id_stmt FROM @idx_sys_user_department_id_sql;
EXECUTE idx_sys_user_department_id_stmt;
DEALLOCATE PREPARE idx_sys_user_department_id_stmt;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    org_id BIGINT NOT NULL DEFAULT 1 COMMENT '组织/租户ID，单组织系统默认1',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码，唯一，用于程序判断，如 admin、dept_admin',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称，展示用',
    data_scope VARCHAR(20) NOT NULL DEFAULT 'self' COMMENT '数据范围：all全部, dept本部门, dept_and_below本部门及下级, self仅本人',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_org_code_deleted (org_id, role_code, is_deleted),
    KEY idx_sys_role_status (org_id, status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级权限ID，0表示根节点',
    perm_code VARCHAR(100) NOT NULL COMMENT '权限编码，唯一，如 okr:objective:create',
    perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    perm_type VARCHAR(20) NOT NULL COMMENT '权限类型：menu菜单, button按钮, api接口',
    path VARCHAR(200) NULL COMMENT '前端路由路径或接口路径',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code_deleted (perm_code, is_deleted),
    KEY idx_sys_permission_parent (parent_id, is_deleted),
    KEY idx_sys_permission_type (perm_type, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
    KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与权限关联表';

CREATE TABLE IF NOT EXISTS okr_objective (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'O主键ID',
    objective_name VARCHAR(200) NOT NULL COMMENT 'Objective名称',
    owner_user_id BIGINT NOT NULL COMMENT '负责人用户ID',
    department_id BIGINT NULL COMMENT 'Objective所属部门ID',
    cycle_id BIGINT NULL COMMENT '所属OKR周期ID',
    progress DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '进度：0-100',
    status VARCHAR(20) NOT NULL DEFAULT 'ongoing' COMMENT 'O状态：draft/ongoing/done/closed',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_okr_objective_owner_user_id (owner_user_id),
    KEY idx_okr_objective_department_id (department_id),
    KEY idx_okr_objective_cycle_id (cycle_id),
    KEY idx_okr_objective_status (status),
    KEY idx_okr_objective_createtime (createtime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='O表';

SET @okr_objective_department_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_objective'
      AND column_name = 'department_id'
);
SET @okr_objective_department_id_sql := IF(
    @okr_objective_department_id_exists = 0,
    'ALTER TABLE okr_objective ADD COLUMN department_id BIGINT NULL COMMENT ''Objective所属部门ID'' AFTER owner_user_id',
    'SELECT 1'
);
PREPARE okr_objective_department_id_stmt FROM @okr_objective_department_id_sql;
EXECUTE okr_objective_department_id_stmt;
DEALLOCATE PREPARE okr_objective_department_id_stmt;

SET @idx_okr_objective_department_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_objective'
      AND index_name = 'idx_okr_objective_department_id'
);
SET @idx_okr_objective_department_id_sql := IF(
    @idx_okr_objective_department_id_exists = 0,
    'CREATE INDEX idx_okr_objective_department_id ON okr_objective(department_id)',
    'SELECT 1'
);
PREPARE idx_okr_objective_department_id_stmt FROM @idx_okr_objective_department_id_sql;
EXECUTE idx_okr_objective_department_id_stmt;
DEALLOCATE PREPARE idx_okr_objective_department_id_stmt;

SET @okr_objective_cycle_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_objective'
      AND column_name = 'cycle_id'
);
SET @okr_objective_cycle_id_sql := IF(
    @okr_objective_cycle_id_exists = 0,
    'ALTER TABLE okr_objective ADD COLUMN cycle_id BIGINT NULL COMMENT ''所属OKR周期ID'' AFTER department_id',
    'SELECT 1'
);
PREPARE okr_objective_cycle_id_stmt FROM @okr_objective_cycle_id_sql;
EXECUTE okr_objective_cycle_id_stmt;
DEALLOCATE PREPARE okr_objective_cycle_id_stmt;

SET @idx_okr_objective_cycle_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_objective'
      AND index_name = 'idx_okr_objective_cycle_id'
);
SET @idx_okr_objective_cycle_id_sql := IF(
    @idx_okr_objective_cycle_id_exists = 0,
    'CREATE INDEX idx_okr_objective_cycle_id ON okr_objective(cycle_id)',
    'SELECT 1'
);
PREPARE idx_okr_objective_cycle_id_stmt FROM @idx_okr_objective_cycle_id_sql;
EXECUTE idx_okr_objective_cycle_id_stmt;
DEALLOCATE PREPARE idx_okr_objective_cycle_id_stmt;

CREATE TABLE IF NOT EXISTS okr_key_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'KR主键ID',
    kr_name VARCHAR(200) NOT NULL COMMENT 'KR名称',
    sort_order INT NOT NULL DEFAULT 1 COMMENT '同一个O下的排序顺序',
    weight DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '权重：0-100',
    completion_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '完成度：0-100',
    objective_id BIGINT NOT NULL COMMENT '关联O ID',
    deadline DATETIME NULL COMMENT 'KR截止时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ongoing' COMMENT 'KR状态：todo/ongoing/done',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_okr_key_result_objective_id (objective_id),
    KEY idx_okr_key_result_status (status),
    KEY idx_okr_key_result_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='KR表';

CREATE TABLE IF NOT EXISTS okr_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务主键ID',
    task_name VARCHAR(200) NOT NULL COMMENT 'Task名称',
    status VARCHAR(20) NOT NULL DEFAULT 'todo' COMMENT '任务状态：todo/ongoing/done/cancel',
    owner_user_id BIGINT NOT NULL COMMENT '归属人用户ID',
    kr_id BIGINT NOT NULL COMMENT '关联KR ID',
    department_id BIGINT NOT NULL COMMENT '归属部门ID',
    priority INT NOT NULL DEFAULT 2 COMMENT '优先级：1低，2中，3高',
    deadline DATETIME NULL COMMENT '任务截止时间',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_okr_task_owner_user_id (owner_user_id),
    KEY idx_okr_task_kr_id (kr_id),
    KEY idx_okr_task_status (status),
    KEY idx_okr_task_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Task表';

CREATE TABLE IF NOT EXISTS okr_objective_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户',
    objective_id BIGINT NOT NULL COMMENT '关联O',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_okr_objective_user (user_id, objective_id),
    KEY idx_okr_objective_user_objective_id (objective_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与O关联表';

CREATE TABLE IF NOT EXISTS okr_task_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户',
    task_id BIGINT NOT NULL COMMENT '关联任务',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_okr_task_user (user_id, task_id),
    KEY idx_okr_task_user_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与Task关联表';

CREATE TABLE IF NOT EXISTS okr_cycle (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '周期ID',
    org_id BIGINT NOT NULL DEFAULT 1 COMMENT '组织/租户ID，单组织系统默认1',
    department_id BIGINT NOT NULL COMMENT '周期所属部门ID',
    cycle_type VARCHAR(20) NOT NULL COMMENT '周期类型：month, quarter, half_year, year, custom',
    cycle_year INT NULL COMMENT '周期年份，例如2026',
    cycle_no INT NULL COMMENT '周期序号：月度1-12，季度1-4，半年度1-2，年度1，自定义为空',
    name VARCHAR(100) NOT NULL COMMENT '周期名称，例如2026年战略目标OKR',
    start_date DATE NOT NULL COMMENT '周期开始日期',
    end_date DATE NOT NULL COMMENT '周期结束日期',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft, enabled, disabled, closed, archived',
    created_by BIGINT NULL COMMENT '创建人',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT NULL COMMENT '更新人',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_okr_cycle_department_type_year_no_deleted (org_id, department_id, cycle_type, cycle_year, cycle_no, is_deleted),
    KEY idx_okr_cycle_department_id (department_id),
    KEY idx_okr_cycle_org_status (org_id, status, is_deleted),
    KEY idx_okr_cycle_date (org_id, department_id, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OKR周期表';

SET @okr_cycle_department_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_cycle'
      AND column_name = 'department_id'
);
SET @okr_cycle_department_id_sql := IF(
    @okr_cycle_department_id_exists = 0,
    'ALTER TABLE okr_cycle ADD COLUMN department_id BIGINT NOT NULL DEFAULT 0 COMMENT ''周期所属部门ID'' AFTER org_id',
    'SELECT 1'
);
PREPARE okr_cycle_department_id_stmt FROM @okr_cycle_department_id_sql;
EXECUTE okr_cycle_department_id_stmt;
DEALLOCATE PREPARE okr_cycle_department_id_stmt;

SET @idx_okr_cycle_department_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_cycle'
      AND index_name = 'idx_okr_cycle_department_id'
);
SET @idx_okr_cycle_department_id_sql := IF(
    @idx_okr_cycle_department_id_exists = 0,
    'CREATE INDEX idx_okr_cycle_department_id ON okr_cycle(department_id)',
    'SELECT 1'
);
PREPARE idx_okr_cycle_department_id_stmt FROM @idx_okr_cycle_department_id_sql;
EXECUTE idx_okr_cycle_department_id_stmt;
DEALLOCATE PREPARE idx_okr_cycle_department_id_stmt;

SET @uk_okr_cycle_old_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_cycle'
      AND index_name = 'uk_okr_cycle_org_type_year_no_deleted'
);
SET @uk_okr_cycle_old_sql := IF(
    @uk_okr_cycle_old_exists > 0,
    'ALTER TABLE okr_cycle DROP INDEX uk_okr_cycle_org_type_year_no_deleted',
    'SELECT 1'
);
PREPARE uk_okr_cycle_old_stmt FROM @uk_okr_cycle_old_sql;
EXECUTE uk_okr_cycle_old_stmt;
DEALLOCATE PREPARE uk_okr_cycle_old_stmt;

SET @uk_okr_cycle_department_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'okr_cycle'
      AND index_name = 'uk_okr_cycle_department_type_year_no_deleted'
);
SET @uk_okr_cycle_department_sql := IF(
    @uk_okr_cycle_department_exists = 0,
    'ALTER TABLE okr_cycle ADD UNIQUE KEY uk_okr_cycle_department_type_year_no_deleted (org_id, department_id, cycle_type, cycle_year, cycle_no, is_deleted)',
    'SELECT 1'
);
PREPARE uk_okr_cycle_department_stmt FROM @uk_okr_cycle_department_sql;
EXECUTE uk_okr_cycle_department_stmt;
DEALLOCATE PREPARE uk_okr_cycle_department_stmt;

CREATE TABLE IF NOT EXISTS okr_objective_alignment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '对齐关系ID',
    objective_id BIGINT NOT NULL COMMENT '当前O ID，即发起对齐的O',
    aligned_objective_id BIGINT NOT NULL COMMENT '对齐的上级O ID',
    aligned_kr_id BIGINT NULL COMMENT '对齐的上级KR ID；为空表示只对齐到O',
    alignment_type VARCHAR(20) NOT NULL DEFAULT 'upward' COMMENT '对齐类型：upward向上对齐',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active有效，cancelled取消',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    updated_by BIGINT NULL COMMENT '更新人ID',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_okr_objective_alignment_active (objective_id, status, is_deleted),
    KEY idx_okr_objective_alignment_objective_id (objective_id),
    KEY idx_okr_objective_alignment_target_objective_id (aligned_objective_id),
    KEY idx_okr_objective_alignment_target_kr_id (aligned_kr_id),
    KEY idx_okr_objective_alignment_created_by (created_by),
    KEY idx_okr_objective_alignment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OKR目标对齐关系表';

CREATE TABLE okr_progress_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type VARCHAR(32) NOT NULL COMMENT 'OBJECTIVE/KR/TASK',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    old_progress DECIMAL(5,2) NULL COMMENT '变更前进度',
    new_progress DECIMAL(5,2) NOT NULL COMMENT '变更后进度',
    source_type VARCHAR(32) NOT NULL COMMENT 'MANUAL/TASK_DERIVATION/SYSTEM',
    operator_id BIGINT NULL COMMENT '操作人ID',
    remark VARCHAR(500) NULL COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_target (target_type, target_id),
    KEY idx_operator_id (operator_id),
    KEY idx_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS okr_check_in (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    objective_id BIGINT NOT NULL COMMENT 'Check-in关联的Objective ID',
    cycle_id BIGINT NULL COMMENT '周期ID',
    check_in_user_id BIGINT NULL COMMENT '提交人ID',
    confidence_level TINYINT NULL COMMENT '信心指数：1-5',
    summary VARCHAR(1000) NULL COMMENT '本次进展总结',
    risk VARCHAR(1000) NULL COMMENT '风险说明',
    blocker VARCHAR(1000) NULL COMMENT '阻塞说明',
    next_plan VARCHAR(1000) NULL COMMENT '下一步计划',
    apply_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '异步应用状态：PENDING/APPLIED/FAILED',
    apply_message VARCHAR(500) NULL COMMENT '异步应用结果说明',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    applied_at DATETIME NULL COMMENT '异步应用完成时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_okr_check_in_objective_id (objective_id),
    KEY idx_okr_check_in_cycle_id (cycle_id),
    KEY idx_okr_check_in_user_id (check_in_user_id),
    KEY idx_okr_check_in_apply_status (apply_status),
    KEY idx_okr_check_in_submitted_at (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OKR Check-in主表';

CREATE TABLE IF NOT EXISTS okr_check_in_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_in_id BIGINT NOT NULL COMMENT 'Check-in主表ID',
    kr_id BIGINT NOT NULL COMMENT 'KR ID',
    old_completion_rate DECIMAL(5,2) NULL COMMENT '提交时KR旧完成度',
    new_completion_rate DECIMAL(5,2) NOT NULL COMMENT '提交的新完成度',
    progress_delta DECIMAL(5,2) NULL COMMENT '完成度变化量',
    remark VARCHAR(1000) NULL COMMENT 'KR进展说明',
    apply_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '异步应用状态：PENDING/APPLIED/FAILED',
    apply_message VARCHAR(500) NULL COMMENT '异步应用结果说明',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_okr_check_in_item_check_in_id (check_in_id),
    KEY idx_okr_check_in_item_kr_id (kr_id),
    KEY idx_okr_check_in_item_apply_status (apply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OKR Check-in明细表';

CREATE TABLE okr_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(64) NOT NULL COMMENT '服务名',
    resource_type VARCHAR(32) NOT NULL COMMENT 'OBJECTIVE/KR/TASK/CYCLE/USER/DEPARTMENT',
    resource_id BIGINT NULL COMMENT '资源ID',
    action VARCHAR(32) NOT NULL COMMENT 'CREATE/UPDATE/DELETE/STATUS_CHANGE/LOGIN',
    operator_id BIGINT NULL COMMENT '操作人ID',
    before_json JSON NULL COMMENT '操作前数据',
    after_json JSON NULL COMMENT '操作后数据',
    request_id VARCHAR(64) NULL COMMENT '请求ID',
    ip VARCHAR(64) NULL COMMENT 'IP地址',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    createtime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_resource (resource_type, resource_id),
    KEY idx_operator_id (operator_id),
    KEY idx_action (action),
    KEY idx_createtime (createtime)
);
