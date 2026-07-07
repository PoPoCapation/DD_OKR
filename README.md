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

---

## 本次迭代完成记录(v2)

### 完成了什么

1. **`leader_user_id` 全链补齐**:之前只有 SQL 层(CTE 递归)能用 leader_user_id,Java 层不通。补齐 `SysUserPO` / `SystemUserVO` 加字段 + `sys_user_mapper.xml`(resultMap + column list + insert + update)+ `UserRepository` 4 处 PO↔VO 映射。Java 层现在能 CRUD 用户的上级。
2. **可见 vs 可编辑权限分离**:
   - `queryVisibleUserIds`(自己 + 上级 + 下级)→ 查询列表(能看)
   - `queryEditableUserIds`(自己 + 下级,不含上级)→ 编辑校验(能改/删)
   - 效果:下级能看上级 OKR 但不能改,上级能改下级,自己能改自己
3. **KR / Task 增删改查**:VO + IService/Service + IRepository/Repository + DAO(补 `queryByObjectiveId` / `queryByKrId`)+ mapper + Controller + DTO,全套 CRUD。数据权限:KR 校验所属 O 可见,Task 校验 KR→O 可见。
4. **KR/Task case 编排(32 文件)**:对齐 O 的模式,4 套 case(`krcreate` / `krquery` / `taskcreate` / `taskquery`),每套 Interface + Service + Factory + Abstract + 4 Node。Controller 的 create/list 改走 case,update/delete 走 service。
5. **case 层重构**:每用例拆独立包(`cases/auth/login`、`cases/okr/krcreate` 等),节点不再混在一个 node 目录(之前 `cases/auth/node` 混了登录 + 注册节点)。
6. **前端美化(Tailwind v4)**:换掉纯 CSS,装 Tailwind(vite 插件 + `@theme` 自定义主色 #3370ff / #25d0a2 / #fd5b5b)。3 页面:对齐视图 / OKRs 列表 / 任务列表,浅色 SaaS 风格(玻璃拟态卡片 + 圆角 + 浅阴影)。
7. **精美登录页 + 前后端合并**:玻璃拟态登录/注册页(渐变背景 + Logo + tab 切换)。登录后进 3 页面,数据从后端拉(不再硬编码)。`request` 封装自动带 `Authorization: Bearer <token>`。
8. **所有按钮绑 API + 菜单精简**:顶部菜单只留 OKRs / 对齐 / 任务 3 个。OKRs 页的创建目标 / 创建 KR / 删除 / 改进度;任务页的查询 / 创建 / 删除;对齐视图点击删除 —— 全部调真实后端接口。任务页从 KR 一键跳转(带 krId 自动查)。

### 怎么完成的(关键改动)

| 模块 | 关键文件 | 改动 |
|---|---|---|
| 数据权限 | `sys_user_mapper.xml` / `ISysUserDao` / `IUserRepository` / `IUserService` / `UserRepository` / `UserService` | 新增 `queryEditableUserIds`(CTE 递归自己 + 下级,不加上级) |
| OKR 业务 | `OkrKeyResultVO` / `OkrTaskVO` + 各自 Service/Repository/DAO/mapper/Controller/DTO | KR/Task 全套 CRUD + 数据权限校验(KR 校验 O 可见,Task 校验 KR→O 可见) |
| case 编排 | `cases/okr/krcreate` / `krquery` / `taskcreate` / `taskquery` | 4 套 case(每套 8 文件:Interface/Service/Factory/Abstract + 4 Node) |
| 前端样式 | `vite.config.ts` / `index.css` / `package.json` | Tailwind v4 插件 + `@theme` 自定义主色 |
| 前端页面 | `components/LoginPage` / `TopNav` / `SideNav` / `ProgressCard` + `pages/AlignmentView` / `OKROverview` / `TaskEmptyState` | 精美登录页 + 3 页面调真实 API + 所有按钮绑接口 |
| 权限分离 | `OkrObjectiveService.checkOwnership` | `queryVisibleUserIds` → `queryEditableUserIds`(编辑校验用可编辑范围) |
| KR 进度修改 | `OKROverview` 的"改进度"按钮 | 调 `/api/okr/keyresult/update {id, completionRate}` |
| 任务页跳转 | `App.tsx` + `OKROverview` + `TaskEmptyState` | KR 行"任务"按钮 → setKrId + 切任务页 → 自动查 |

### 关键设计:可见 ≠ 可编辑

```
查询列表(能看):  queryVisibleUserIds  = 自己 + 上级 + 下级
编辑校验(能改):  queryEditableUserIds = 自己 + 下级(不含上级)
```

SQL 区别:`queryVisibleUserIds` 多一个 `UNION SELECT leader_user_id`(加上级),`queryEditableUserIds` 没有。

### 前端按钮 → 后端 API 映射

| 页面 | 按钮 | 接口 |
|---|---|---|
| 登录页 | 登录 / 注册 | `/api/login` / `/api/register` |
| 对齐视图 | 点击目标卡片 | `/api/okr/objective/delete` |
| OKRs 列表 | + 添加目标 | `/api/okr/objective/create` |
| OKRs 列表 | 删除目标 | `/api/okr/objective/delete` |
| OKRs 列表 | + KR | `/api/okr/keyresult/create` |
| OKRs 列表 | 改进度 | `/api/okr/keyresult/update` |
| OKRs 列表 | 删除 KR | `/api/okr/keyresult/delete` |
| OKRs 列表 | 任务(跳转) | 切任务页 + 带 krId |
| 任务列表 | 查询 | `/api/okr/task/list` |
| 任务列表 | + 创建任务 | `/api/okr/task/create` |
| 任务列表 | 删除任务 | `/api/okr/task/delete` |
