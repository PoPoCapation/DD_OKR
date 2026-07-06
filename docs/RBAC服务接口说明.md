# RBAC 服务接口说明

> 本文档说明 `domain/user/service` 下 5 个服务接口的职责、方法、使用方式与典型业务流程。
> 所有服务位于 `cn.bugstack.domain.user.service` 包,实现类在 `cn.bugstack.domain.user.service.{user|role|permission|department}` 子包。

---

## 一、总体架构

这 5 个服务覆盖 RBAC(Role-Based Access Control,基于角色的访问控制)的全部业务:用户、角色、权限、部门,以及它们之间的关联与鉴权查询。

```
┌─────────────────────────────────────────────────────────────┐
│                       Trigger 层(Controller)                │
│            注入下方 service,提供 HTTP 接口                      │
└───────────────────────────┬─────────────────────────────────┘
                            │ 调用
┌───────────────────────────▼─────────────────────────────────┐
│                      Domain 层(Service)                     │
│                                                             │
│   IUserService ──┐                                           │
│   IUserRoleService ─┤  用户-角色关联 + 鉴权查询(角色码/权限码)│
│   IRoleService ──┤  角色 CRUD + 角色-权限绑定                │
│   IPermissionService ─┤  权限 CRUD                           │
│   IDepartmentService ┘  部门 CRUD                            │
└───────────────────────────┬─────────────────────────────────┘
                            │ 调用
┌───────────────────────────▼─────────────────────────────────┐
│                  Infrastructure 层(Repository)              │
│   UserRepository / RoleRepository / PermissionRepository ... │
│   → DAO(ISysUserDao 等)→ MyBatis Mapper → DB               │
└─────────────────────────────────────────────────────────────┘
```

**数据表对应关系:**

| 表 | PO | 服务 |
|---|---|---|
| sys_user | SysUserPO | IUserService |
| sys_user_role | SysUserRolePO | IUserRoleService |
| sys_role | SysRolePO | IRoleService |
| sys_role_permission | SysRolePermissionPO | IRoleService(角色-权限绑定) |
| sys_permission | SysPermissionPO | IPermissionService |
| sys_department | SysDepartmentPO | IDepartmentService |

---

## 二、各服务详解

### 1. IUserService — 用户管理

**职责:** 用户的增删改查。一个用户 = 一条 `sys_user` 记录。

**方法:**

| 方法 | 入参 | 返回 | 说明 |
|---|---|---|---|
| `createUser` | `SystemUserVO` | `void` | 创建用户 |
| `updateUser` | `SystemUserVO` | `void` | 更新用户 |
| `deleteUser` | `Long userId` | `void` | 逻辑删除用户(is_deleted=1) |
| `queryUserByUserId` | `Long userId` | `SystemUserVO` | 按 ID 查询;查不到抛 `USER_FIND_FAIL` |

**使用示例:**

```java
@Resource
private IUserService userService;

// 创建用户
SystemUserVO user = SystemUserVO.builder()
        .username("张三")
        .account("zhangsan")
        .password("加密后的密码")
        .departmentId(1L)
        .status(1)
        .isDeleted(0)
        .createtime(new Date())
        .updatetime(new Date())
        .build();
userService.createUser(user);   // 失败抛 AppException(USER_CREATE_FAIL)

// 查询用户
SystemUserVO db = userService.queryUserByUserId(100L);
```

---

### 2. IUserRoleService — 用户角色关联 + 鉴权查询

**职责:** 管理用户与角色的关联(给用户分配/取消角色),以及**鉴权查询**(登录后取用户角色码、权限码)。

这是 5 个服务里最常用的,既管"分配"又管"鉴权"。

**方法:**

| 方法 | 入参 | 返回 | 说明 |
|---|---|---|---|
| `setUserRoles` | `userId, List<roleId>` | `void` | **全量覆盖**:以传入列表为准,移除多余的、新增缺失的。事务 |
| `addUserRoles` | `userId, List<roleId>` | `void` | 增量添加(已存在的自动去重)。事务 |
| `removeUserRoles` | `userId, List<roleId>` | `void` | 移除指定角色。事务 |
| `queryRoleIdsByUserId` | `userId` | `List<Long>` | 用户的角色 ID 列表(编辑回显用,不过滤角色状态) |
| `queryRoleCodesByUserId` | `userId` | `List<String>` | 用户的角色编码列表(鉴权用,仅启用未删除角色) |
| `queryPermissionCodesByUserId` | `userId` | `List<String>` | 用户的权限编码列表(**联三表** user_role+role_permission+permission,鉴权用) |

> `set` vs `add`:`setUserRoles` 是"设置为这些角色"(全量覆盖),`addUserRoles` 是"再加这些角色"(增量)。
> 编辑用户角色页保存时通常用 `set`;单独勾选追加用 `add`。

**使用示例:**

```java
@Resource
private IUserRoleService userRoleService;

// 给用户 100L 设置角色 [1, 2, 3](全量覆盖,原来有 4 会被移除)
userRoleService.setUserRoles(100L, Arrays.asList(1L, 2L, 3L));

// 登录后加载鉴权数据(塞进 JWT 或 session)
List<String> roleCodes = userRoleService.queryRoleCodesByUserId(100L);       // ["admin","dept_admin"]
List<String> permCodes = userRoleService.queryPermissionCodesByUserId(100L); // ["okr:objective:create", ...]
```

---

### 3. IRoleService — 角色管理

**职责:** 角色的增删改查,以及**角色与权限的绑定**(给角色分配权限)。

**方法:**

| 方法 | 入参 | 返回 | 说明 |
|---|---|---|---|
| `createRole` | `SystemRoleVO` | `void` | 创建角色 |
| `updateRole` | `SystemRoleVO` | `void` | 更新角色 |
| `deleteRole` | `Long roleId` | `void` | 逻辑删除角色 |
| `queryRoleByRoleId` | `Long roleId` | `SystemRoleVO` | 按 ID 查询;查不到抛 `ROLE_FIND_FAIL` |
| `setRolePermissions` | `roleId, List<permissionId>` | `void` | 全量覆盖角色的权限。事务 |
| `addRolePermissions` | `roleId, List<permissionId>` | `void` | 增量添加权限(去重)。事务 |
| `removeRolePermissions` | `roleId, List<permissionId>` | `void` | 移除指定权限。事务 |
| `queryPermissionIdsByRoleId` | `roleId` | `List<Long>` | 角色的权限 ID 列表(编辑回显) |

**使用示例:**

```java
@Resource
private IRoleService roleService;

// 创建角色
SystemRoleVO role = SystemRoleVO.builder()
        .orgId(1L)
        .roleCode("dept_admin")
        .roleName("部门管理员")
        .dataScope("dept")          // all/dept/dept_and_below/self
        .sortOrder(2)
        .status(1)
        .isDeleted(0)
        .createtime(new Date())
        .updatetime(new Date())
        .build();
roleService.createRole(role);

// 给角色 1 绑定权限 [10, 11, 12]
roleService.setRolePermissions(1L, Arrays.asList(10L, 11L, 12L));

// 编辑角色权限页回显:查角色已有的权限 ID
List<Long> permIds = roleService.queryPermissionIdsByRoleId(1L);
```

---

### 4. IPermissionService — 权限管理

**职责:** 权限(菜单/按钮/API)的增删改查。权限是 RBAC 的最细粒度单元,通过 `permCode`(如 `okr:objective:create`)在代码里做鉴权判断。

**方法:**

| 方法 | 入参 | 返回 | 说明 |
|---|---|---|---|
| `createPermission` | `SystemPermissionVO` | `void` | 创建权限 |
| `updatePermission` | `SystemPermissionVO` | `void` | 更新权限 |
| `deletePermission` | `Long permissionId` | `void` | 逻辑删除权限 |
| `queryPermissionByPermissionId` | `Long permissionId` | `SystemPermissionVO` | 按 ID 查询;查不到抛 `PERMISSION_FIND_FAIL` |

**使用示例:**

```java
@Resource
private IPermissionService permissionService;

SystemPermissionVO perm = SystemPermissionVO.builder()
        .parentId(0L)               // 0 表示根节点
        .permCode("okr:objective:create")
        .permName("创建目标")
        .permType("button")         // menu / button / api
        .path("/okr/objective/create")
        .sortOrder(1)
        .status(1)
        .isDeleted(0)
        .createtime(new Date())
        .updatetime(new Date())
        .build();
permissionService.createPermission(perm);
```

---

### 5. IDepartmentService — 部门管理

**职责:** 部门(组织架构)的增删改查。部门用于**数据权限范围**(角色的 `dataScope` = dept/dept_and_below 时,按用户所属部门限定可见数据)。

**方法:**

| 方法 | 入参 | 返回 | 说明 |
|---|---|---|---|
| `createDepartment` | `SystemDepartmentVO` | `void` | 创建部门 |
| `updateDepartment` | `SystemDepartmentVO` | `void` | 更新部门 |
| `deleteDepartment` | `Long departmentId` | `void` | 逻辑删除部门 |
| `queryDepartmentByDepartmentId` | `Long departmentId` | `SystemDepartmentVO` | 按 ID 查询;查不到抛 `DEPARTMENT_FIND_FAIL` |

**使用示例:**

```java
@Resource
private IDepartmentService departmentService;

SystemDepartmentVO dept = SystemDepartmentVO.builder()
        .orgId(1L)
        .parentId(0L)               // 0 表示根部门
        .deptName("研发一部")
        .deptCode("RD_01")
        .leaderUserId(100L)         // 部门负责人
        .sortOrder(1)
        .status(1)
        .isDeleted(0)
        .createtime(new Date())
        .updatetime(new Date())
        .build();
departmentService.createDepartment(dept);
```

---

## 三、统一约定(所有服务通用)

### 1. 异常处理

- 业务失败统一抛 `AppException`(extends `RuntimeException`),构造:`new AppException(code, info)`。
- 错误码定义在 `ResponseCode` 枚举,前缀对应模块:`USER_*` / `USER_ROLE_*` / `ROLE_*` / `PERMISSION_*` / `DEPARTMENT_*`。
- **不需要在调用方 try-catch** —— 它是 RuntimeException,建议在 Trigger 层用全局异常处理器(`@RestControllerAdvice`)统一捕获,转成标准 `Response` 返回前端。

```java
// 全局异常处理示例(trigger 层)
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public Response<?> handle(AppException e) {
        return Response.builder()
                .code(e.getCode())
                .info(e.getInfo())
                .build();
    }
}
```

### 2. 事务

- `setXxx` / `addXxx` / `removeXxx`(关联类的批量写操作)在 service 实现类上标了 `@Transactional`,内部多条 SQL(查→删→插)原子提交,失败回滚。
- 单条 CRUD(`create/update/delete`)不需要事务(单条 SQL 自带原子性),未加。
- **注意 `@Transactional` 失效场景:** 不能在同一个 service 类内部用 `this.xxx()` 自调用事务方法(绕过 AOP 代理)。跨 service / 跨 bean 调用正常。

### 3. 返回值约定

| 场景 | 返回 | 说明 |
|---|---|---|
| 单条写(create/update/delete) | `void` | 失败抛异常,成功无返回 |
| 关联批量写(set/add/remove) | `void` | 失败抛异常,成功无返回 |
| 单条查询(queryXxxById) | `VO` | 查不到抛 `*_FIND_FAIL` |
| 列表查询(queryXxxIds/queryXxxCodes) | `List` | 空列表也是合法结果(如用户没角色),**不抛异常** |

### 4. 空校验现状(待统一)

| Service | 空列表入参 | userId/roleId 校验 |
|---|---|---|
| IUserService | — | 无 |
| IUserRoleService | 空列表抛 `ILLEGAL_PARAMETER` | 查询有、写无 |
| IRoleService | 不校验(支持 `set([])` 清空) | 无 |
| IPermissionService | — | 无 |
| IDepartmentService | — | 无 |

> 目前不统一。`IRoleService` 等新写的支持 `setRolePermissions(roleId, [])` = 清空角色所有权限;`IUserRoleService` 是空列表抛异常。需统一成一种。

---

## 四、典型业务流程

### 流程 1:用户登录 → 加载权限 → 签发 JWT

```java
@Resource private IUserService userService;
@Resource private IUserRoleService userRoleService;

public LoginResponse login(String account, String password) {
    // 1. 校验账号密码(此处省略,假设拿到 userId)
    SystemUserVO user = ...; // 按 account 查到用户
    Long userId = user.getId();

    // 2. 加载角色码 + 权限码
    List<String> roleCodes = userRoleService.queryRoleCodesByUserId(userId);
    List<String> permCodes = userRoleService.queryPermissionCodesByUserId(userId);

    // 3. 塞进 JWT,返回前端
    String token = JWT.create(userId, roleCodes, permCodes);
    return new LoginResponse(token, roleCodes, permCodes);
}
```

### 流程 2:管理员在"编辑用户角色"页保存

```java
// 前端提交:用户 100L 的角色勾选为 [1, 2, 3]
userRoleService.setUserRoles(100L, Arrays.asList(1L, 2L, 3L));
// 内部:查旧 [1,4] → diff → 移除 4、新增 2,3,事务提交
```

### 流程 3:管理员在"编辑角色权限"页保存

```java
// 角色 1 的权限勾选为 [10, 11, 12]
roleService.setRolePermissions(1L, Arrays.asList(10L, 11L, 12L));
```

### 流程 4:接口鉴权(后端拦截器/注解)

```java
// 从当前请求的 JWT 解出 permCodes
List<String> userPerms = ...;
if (!userPerms.contains("okr:objective:create")) {
    throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无权限");
}
// 或用 Spring Security @PreAuthorize("hasAuthority('okr:objective:create')")
```

---

## 五、调用关系总览

```
登录/鉴权     → IUserRoleService.queryRoleCodesByUserId
              → IUserRoleService.queryPermissionCodesByUserId  (联三表)

用户管理页    → IUserService (CRUD)
              → IUserRoleService.setUserRoles (分配角色)

角色管理页    → IRoleService (CRUD)
              → IRoleService.setRolePermissions (绑权限)

权限管理页    → IPermissionService (CRUD)

部门管理页    → IDepartmentService (CRUD)
```

---

## 六、文件位置速查

| 类型 | 路径 |
|---|---|
| 接口 | `domain/user/service/I*Service.java` |
| 实现 | `domain/user/service/{user,role,permission,department}/*Service.java` |
| Repository 接口 | `domain/user/adapter/repository/I*Repository.java` |
| Repository 实现 | `infrastructure/adapter/repository/*Repository.java` |
| VO | `domain/user/model/entity/System*VO.java` |
| DAO | `infrastructure/dao/ISys*Dao.java` |
| Mapper XML | `app/src/main/resources/mybatis/mapper/sys_*_mapper.xml` |
| PO | `infrastructure/dao/po/Sys*PO.java` |
| 错误码 | `types/enums/ResponseCode.java` |
| 异常 | `types/exception/AppException.java` |
