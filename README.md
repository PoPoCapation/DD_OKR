# OKR_REPLACE — OKR 目标管理系统

基于 DDD 分层 + RBAC 权限 + 汇报关系数据权限 + case 责任链编排。

## 技术栈

- **后端**:Spring Boot 3.4.3 + MyBatis + MySQL 8 + JWT(jjwt 0.9.1) + BCrypt(spring-security-crypto) + xfg-wrench 责任链框架
- **前端**:React 19 + Vite + TypeScript
- **Java 17**

## 模块结构

| 模块 | 职责 |
|---|---|
| OKR_REPLACE-api | DTO + 统一响应 Response |
| OKR_REPLACE-app | 启动类 + 配置 + MyBatis mapper xml |
| OKR_REPLACE-domain | 领域服务(user 用户/权限,activity OKR) |
| OKR_REPLACE-trigger | controller + JWT 拦截器 + 全局异常处理 |
| OKR_REPLACE-infrastructure | repository 实现 + DAO + PO |
| OKR_REPLACE-types | 工具(PasswordEncoder) + 错误码(ResponseCode) + 异常(AppException) |
| OKR_REPLACE-case | 用例编排层(责任链节点) |
| front | React 前端 |

调用链路:`trigger(controller) → case(编排) → domain(service) → infrastructure(repository) → DAO → mapper → DB`

---

## 已完成

### 1. RBAC 权限体系

- 用户 / 角色 / 权限 / 部门 CRUD(domain service + repository + DAO + mapper)
- 用户-角色关联:`setUserRoles` / `addUserRoles` / `removeUserRoles` + 查询角色ID/角色码
- 角色-权限关联:`setRolePermissions` / `addRolePermissions` / `removeRolePermissions` + 查询权限ID
- **角色继承**:`sys_role.parent_id` 链(老板→主管→组长→员工),上层自动拥有下层全部权限,递归 CTE 查询合并
- 4 个预设角色:老板(`all`)/ 主管(`dept_and_below`)/ 组长(`dept`)/ 员工(`self`)

### 2. 登录闭环

- 注册 `/api/register`(注册即登录,返回 token)
- 登录 `/api/login`(验密码 + 加载角色/权限码 + 签 JWT)
- JWT 拦截器(校验 token,解 userId 放 request 属性)
- 全局异常处理(`@RestControllerAdvice`,AppException → 标准 Response)
- 密码 BCrypt 加密存储

### 3. OKR 目标 + 数据权限(汇报关系)

- 目标 CRUD:创建 / 更新 / 删除 / 查询列表
- **数据权限:汇报关系模型**(替换部门 data_scope):
  - `sys_user.leader_user_id`(直属上级),CTE 递归查可见用户 = 自己 + 直属上级 + 全部下级递归
  - 查询目标:`WHERE owner_user_id IN (可见用户列表)`
  - 删除/更新:校验目标负责人在可见用户范围内
- 前端目标管理页(列表 / 创建 / 删除)

### 4. case 编排层(参考 ai-mcp-gateway)

基于 `xfg-wrench` 的 `AbstractMultiThreadStrategyRouter` 责任链节点编排:

| 用例 | 节点链 |
|---|---|
| 登录 | Root → 验密码 → 加载权限 → 签JWT → End |
| 注册 | Root → 查重 → 创建用户 → 加载权限 → 签JWT → End |
| 创建目标 | Root → 设owner/dept → 创建 → End |
| 查询目标 | Root → 算可见用户 → 查目标 → End |

每个用例一套:`IXxxCase`(接口) → `XxxCaseService`(调 Factory) → `XxxCaseFactory`(持 RootNode + Context) → `AbstractXxxCaseSupport`(节点基类,注入 domain service) → Node 链(Root→...→End)。

### 5. 前端(React)

- 登录 / 注册页(注册即登录)
- 权限管理(创建 / 查询 / 删除权限)
- 目标管理(列表 / 创建 / 删除,按数据权限过滤)
- token 存 localStorage,请求自动带 `Authorization: Bearer <token>`

---

## 接下来

### OKR 业务完善

- [ ] 关键结果(KR)CRUD + 进度计算(KR 进度汇总到 O)
- [ ] 任务(Task)CRUD + 任务-用户关联
- [ ] 目标对齐(alignment,目标对齐到上级目标)
- [ ] 进度记录(progress_record,更新历史)
- [ ] 复盘(check_in,周期复盘)
- [ ] 操作日志(operation_log,审计)

### case 编排扩展

- [ ] 目标 `update` / `delete` case 编排(目前仍走 `IOKRObjectiveService`)
- [ ] KR / 任务等新用例的 case 编排

### 后台管理 controller

- [ ] `RoleController`(角色 CRUD + 角色-权限绑定 UI)
- [ ] `UserRoleController`(用户-角色分配 UI)
- [ ] `DepartmentController`(部门 CRUD + 部门树)
- [ ] `UserController`(用户 CRUD,目前只能注册)

### 接口鉴权

- [ ] `@RequirePermission` 注解(基于 JWT 权限码,细粒度接口鉴权)
- [ ] 或接入 Spring Security `@PreAuthorize`

### 工程化

- [ ] 数据库配置正式化(本地/生产分离,脱离远程 `group_buy_market`)
- [ ] 单元测试 + 集成测试
- [ ] 接口文档(Swagger / OpenAPI)
- [ ] Redis 恢复(取消 `@ConditionalOnProperty`,补 `setPassword`)
- [ ] CI/CD

---

## 文档

- `docs/RBAC服务接口说明.md` — 5 个 service 接口详解 + 使用示例
- `docs/rbac-tables.sql` — RBAC 6 张表建表脚本
- `docs/rbac-seed.sql` — 角色继承 + 权限 + 用户分角色种子
- `docs/okr-seed.sql` — 部门 + cb + 示例目标
- `docs/okr-hierarchy-seed.sql` — 汇报关系(leader_user_id)种子

---

## 关键设计

### 数据权限:汇报关系(自己 + 上级 + 下级)

```sql
-- 可见用户 = 自己 + 直属上级 + 全部下级递归
WITH RECURSIVE sub_tree AS (
  SELECT id FROM sys_user WHERE id = #{userId}
  UNION ALL
  SELECT u.id FROM sys_user u JOIN sub_tree t ON u.leader_user_id = t.id
)
SELECT id FROM sub_tree
UNION
SELECT leader_user_id FROM sys_user WHERE id = #{userId} AND leader_user_id IS NOT NULL
```

### 角色继承(上层拥有下层全部权限)

```sql
-- 用户权限 = 用户角色 + 沿 parent_id 递归的被继承角色 的权限
WITH RECURSIVE role_tree AS (
  SELECT id, parent_id FROM sys_role WHERE id IN (用户的角色)
  UNION ALL
  SELECT r.id, r.parent_id FROM sys_role r JOIN role_tree t ON r.id = t.parent_id
)
SELECT DISTINCT perm_code WHERE role_id IN (SELECT id FROM role_tree)
```
