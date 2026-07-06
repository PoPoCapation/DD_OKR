-- ============================================================
-- OKR 目标种子数据:建部门 + 给 cb 设部门 + 示例目标
-- 在 group_buy_market 库执行
-- 前提:cb 已注册(userId=2)、已分组长角色(data_scope=dept)
-- ============================================================

-- 1. 建两个部门
INSERT INTO sys_department (org_id, parent_id, dept_name, dept_code, sort_order, status) VALUES
  (1, 0, '研发部', 'RD', 1, 1),
  (1, 0, '产品部', 'PD', 2, 1);

-- 2. 给 cb 设部门为"研发部"
UPDATE sys_user SET department_id = (SELECT id FROM (SELECT id FROM sys_department WHERE dept_code='RD') t) WHERE account='cb';

-- 3. 建示例目标（属于研发部，负责人=cb）
INSERT INTO okr_objective (objective_name, owner_user_id, department_id, cycle_id, progress, status, is_deleted, createtime, updatetime) VALUES
  ('Q3 研发目标',   2, (SELECT id FROM (SELECT id FROM sys_department WHERE dept_code='RD') t), NULL, 30, 'ongoing', 0, NOW(), NOW()),
  ('重构核心模块', 2, (SELECT id FROM (SELECT id FROM sys_department WHERE dept_code='RD') t), NULL, 10, 'draft',   0, NOW(), NOW());
