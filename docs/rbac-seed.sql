-- ============================================================
-- RBAC 种子数据:角色继承 + 示例权限 + 给 cb 分角色
-- 前提:sys_role 已有四个角色(boss/manager/leader/staff)
-- 在 group_buy_market 库执行
-- ============================================================

-- 1. sys_role 加 parent_id 字段(角色继承:上层继承下层全部权限)
--    若已加过会报错,忽略即可
ALTER TABLE sys_role ADD COLUMN parent_id BIGINT DEFAULT NULL COMMENT '继承的角色ID(上层继承下层全部权限)' AFTER org_id;

-- 2. 设继承链:老板 → 主管 → 组长 → 员工
--    parent_id 指向"被继承的下级角色":boss 继承 manager,manager 继承 leader,leader 继承 staff
UPDATE sys_role SET parent_id = (SELECT id FROM (SELECT id FROM sys_role WHERE role_code='manager') t) WHERE role_code='boss';
UPDATE sys_role SET parent_id = (SELECT id FROM (SELECT id FROM sys_role WHERE role_code='leader') t) WHERE role_code='manager';
UPDATE sys_role SET parent_id = (SELECT id FROM (SELECT id FROM sys_role WHERE role_code='staff') t) WHERE role_code='leader';
-- staff(员工)parent_id 保持 NULL,最底层

-- 3. 建示例权限(4 个 OKR 操作)
INSERT INTO sys_permission (parent_id, perm_code, perm_name, perm_type, path, sort_order, status, remark) VALUES
  (0, 'okr:objective:view',    '查看目标', 'api', '/api/okr/objective/query',   1, 1, '查看 OKR 目标'),
  (0, 'okr:objective:create',  '创建目标', 'api', '/api/okr/objective/create',  2, 1, '创建 OKR 目标'),
  (0, 'okr:objective:approve', '审批目标', 'api', '/api/okr/objective/approve', 3, 1, '审批 OKR 目标'),
  (0, 'okr:objective:delete',  '删除目标', 'api', '/api/okr/objective/delete',  4, 1, '删除 OKR 目标');

-- 4. 给角色绑权限(只绑各角色"额外"的,上层通过继承自动拥有下层权限)
--    员工:查看 + 创建(基础权限)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code='staff' AND p.perm_code IN ('okr:objective:view','okr:objective:create');

--    组长:额外 +审批(继承员工 → 自动有 view+create,再加 approve)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code='leader' AND p.perm_code='okr:objective:approve';

--    老板:额外 +删除(继承主管→组长→员工 → 自动有 view+create+approve,再加 delete)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code='boss' AND p.perm_code='okr:objective:delete';

-- 5. 给用户 cb 分配"组长"角色(假设 cb 已注册存在)
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.account='cb' AND r.role_code='leader';
