-- ============================================================
-- 汇报关系数据权限种子数据
-- 前提:已用前端注册 cb、xiaoli、laowang 三个账号(密码都 123456)
--       cb=组长, xiaoli=组员(cb 下级), laowang=主管(cb 上级)
--       已执行 okr-seed.sql(有研发部 RD, cb 部门=研发部)
-- ============================================================

-- 1. sys_user 加 leader_user_id 字段(直属上级用户ID)
ALTER TABLE sys_user ADD COLUMN leader_user_id BIGINT DEFAULT NULL COMMENT '直属上级用户ID(汇报关系)' AFTER department_id;

-- 2. 设汇报关系: cb 的上级=laowang, xiaoli 的上级=cb
UPDATE sys_user SET leader_user_id = (SELECT id FROM (SELECT id FROM sys_user WHERE account='laowang') t) WHERE account='cb';
UPDATE sys_user SET leader_user_id = (SELECT id FROM (SELECT id FROM sys_user WHERE account='cb') t) WHERE account='xiaoli';

-- 3. 给 xiaoli/laowang 设部门(研发部)+ 分角色(员工/主管)
UPDATE sys_user SET department_id = (SELECT id FROM (SELECT id FROM sys_department WHERE dept_code='RD') t) WHERE account IN ('xiaoli','laowang');
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.account='xiaoli' AND r.role_code='staff';
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.account='laowang' AND r.role_code='manager';

-- 4. 建不同负责人的目标(验证 cb 能看到上级+下级的)
INSERT INTO okr_objective (objective_name, owner_user_id, department_id, progress, status, is_deleted, createtime, updatetime) VALUES
  ('主管老王的目标', (SELECT id FROM (SELECT id FROM sys_user WHERE account='laowang') t), (SELECT id FROM (SELECT id FROM sys_department WHERE dept_code='RD') t), 50, 'ongoing', 0, NOW(), NOW()),
  ('组员小李的目标', (SELECT id FROM (SELECT id FROM sys_user WHERE account='xiaoli') t), (SELECT id FROM (SELECT id FROM sys_department WHERE dept_code='RD') t), 20, 'ongoing', 0, NOW(), NOW());
