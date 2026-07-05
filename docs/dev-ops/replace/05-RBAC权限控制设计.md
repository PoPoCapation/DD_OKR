# 第 5 章 · RBAC 权限控制设计（核心）

> OKR_REPLACE 迁移与 DDD 领域事件架构设计文档 · 第 5 章
> ⬅ 上一章：[第 4 章 领域事件驱动设计模式](04-领域事件驱动设计模式.md) ｜ ➡ 下一章：[第 6 章 分层落地详解](06-分层落地详解.md)

---

## 5.1 OKR 现状与目标

| 维度 | OKR 现状 | OKR_REPLACE 目标 |
| --- | --- | --- |
| 表 | 五表已建未启用 | **完整启用**五表 + 种子数据 |
| 认证 | JWT（jjwt），网关统一校验，透传头 | JWT（复用已声明的 jjwt 0.9.1 / java-jwt 4.4.0），trigger 拦截器校验 |
| 鉴权 | `PermissionService` 编程式 `if`，散落各 Service | **声明式** `@RequirePermission("okr:objective:create")` 注解 + AOP |
| 数据权限 | 部分接入 `canManageDept` | **`data_scope`**（all/dept/dept_and_below/self）统一拦截 |
| 用户上下文 | `SecurityContext`(ThreadLocal) + 透传头 | `UserContext`(ThreadLocal)，拦截器从 JWT 填充 |
| 鉴权位置 | 网关 Filter + Service 内 | **trigger 拦截器 + AOP**（不污染 domain/case） |

## 5.2 RBAC 模型

```
sys_user ──< sys_user_role >── sys_role ──< sys_role_permission >── sys_permission
   │                              │
   │                              └── data_scope: all / dept / dept_and_below / self
   │
   └── department_id ──> sys_department (树形，parent_id)
```

- **用户-角色**：多对多（`sys_user_role`）。
- **角色-权限**：多对多（`sys_role_permission`）。
- **角色** 携带 `data_scope`（数据范围），决定该角色能看到哪些数据。
- **权限点** `sys_permission.perm_code` 形如 `okr:objective:create`，`perm_type` 分 `menu/button/api`。
- **部门树**：`sys_department.parent_id`，`data_scope=dept_and_below` 时需递归取下级部门。

## 5.3 三层鉴权

```
请求 ──> ① 认证(JwtAuthInterceptor) ──> ② 鉴权(@RequirePermission AOP) ──> ③ 数据权限(data_scope 拦截) ──> 业务
         解析JWT→UserContext           校验perm_code∈用户权限集            查询时按dept scope过滤
         失败→401                      失败→403                          失败→空集/403
```

| 层 | 触发点 | 失败码 | 说明 |
| --- | --- | --- | --- |
| ① 认证 | `JwtAuthInterceptor`（preHandle） | 401 | 白名单：`/api/auth/login`、`/api/auth/register`、swagger、`/api/health` |
| ② 鉴权 | `@RequirePermission` 注解 + AOP 环绕 | 403 | Controller 方法或类上声明所需 `perm_code` |
| ③ 数据权限 | case 层调 `IPermissionService` 拿 `manageableDeptIds` 过滤 | 空集 | 查询类接口按部门范围过滤；写类接口先校验 `canManage` |

## 5.4 鉴权归属层（关键设计）

- **`UserContext`**：放 `trigger/interceptor/`，ThreadLocal 持有当前用户。`LoginUser` 值对象定义在 `domain/iam/model/valobj/LoginUserVO`（保持 domain 纯净，trigger 引用 domain）。
- **`JwtAuthInterceptor`**：`trigger/interceptor/`，解析 JWT 填充 `UserContext`。
- **`@RequirePermission` + `RequirePermissionAspect`**：注解 + 切面均放 `trigger/aspect/`。
- **`IPermissionService`**：接口放 `domain/iam/service/`（领域服务），实现放 `infrastructure`（查五表）。
- **`IJwtGateway`**：JWT 签发/解析端口接口放 `domain/iam/adapter/gateway/`，实现放 `infrastructure/adapter/gateway/`（端口反转，domain 不耦合 jjwt）。

> 为什么鉴权放 trigger 而非 case/domain？鉴权是"接入控制"，属于 trigger 的职责（ai-mcp-gateway 把校验放 case 是因为它没分层鉴权概念，本次改进）。domain/case 保持纯净，只关心业务；权限不通过直接抛 `AppException`，由 trigger 全局异常处理转 403。

## 5.5 完整代码示例

### 5.5.1 登录用户值对象（domain 层，保持纯净）

`domain/iam/model/valobj/LoginUserVO.java`：

```java
package cn.bugstack.domain.iam.model.valobj;

import java.util.Set;

/**
 * 当前登录用户值对象（不可变）。
 * 定义在 domain，trigger 的 UserContext 引用它，避免 domain 反向依赖 trigger。
 */
public record LoginUserVO(
        Long userId,
        String account,
        String username,
        Long departmentId,
        Set<String> roles,         // role_code 集合
        Set<String> permissions    // perm_code 集合
) {
    public boolean isAdmin() { return roles != null && roles.contains("admin"); }
}
```

### 5.5.2 用户上下文（trigger 层）

`trigger/interceptor/UserContext.java`：

```java
package cn.bugstack.trigger.interceptor;

import cn.bugstack.domain.iam.model.valobj.LoginUserVO;
import java.util.Set;

/**
 * 当前登录用户上下文（ThreadLocal）。
 * JwtAuthInterceptor 解析 JWT 后填充；请求结束清理。
 */
public class UserContext {

    private static final ThreadLocal<LoginUserVO> HOLDER = new ThreadLocal<>();

    public static void set(LoginUserVO user) { HOLDER.set(user); }
    public static LoginUserVO get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    public static Long getUserId() {
        LoginUserVO u = get();
        return u == null ? null : u.userId();
    }

    public static Set<String> getPermissions() {
        LoginUserVO u = get();
        return u == null ? Set.of() : u.permissions();
    }
}
```

### 5.5.3 JWT 认证拦截器（trigger 层）

`trigger/interceptor/JwtAuthInterceptor.java`：

```java
package cn.bugstack.trigger.interceptor;

import cn.bugstack.domain.iam.adapter.gateway.IJwtGateway;
import cn.bugstack.domain.iam.model.valobj.LoginUserVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * JWT 认证拦截器。
 * 解析 Authorization: Bearer <token>，校验后填充 UserContext。
 * IJwtGateway 是 domain 端口，实现在 infrastructure（端口反转，domain 不耦合 jjwt）。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Resource
    private IJwtGateway jwtGateway;

    private static final Set<String> WHITELIST = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/health",
            "/doc.html", "/swagger-resources", "/v3/api-docs", "/webjars/"
    );

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String path = req.getRequestURI();
        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            return true;
        }
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new AppException(ResponseCode.UNAUTHORIZED.getCode(), "未登录");
        }
        String token = auth.substring(7);
        LoginUserVO user = jwtGateway.verify(token);
        if (user == null) {
            throw new AppException(ResponseCode.UNAUTHORIZED.getCode(), "token无效或已过期");
        }
        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object h, Exception ex) {
        UserContext.clear();  // 防止 ThreadLocal 泄漏
    }
}
```

`app/config/WebMvcConfig.java`（注册拦截器）：

```java
package cn.bugstack.config;

import cn.bugstack.trigger.interceptor.JwtAuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login", "/api/auth/register",
                        "/api/health", "/doc.html", "/swagger-resources/**",
                        "/v3/api-docs/**", "/webjars/**"
                );
    }
}
```

### 5.5.4 鉴权注解 + AOP（trigger 层，声明式）

`trigger/aspect/RequirePermission.java`：

```java
package cn.bugstack.trigger.aspect;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /** 权限编码，如 okr:objective:create */
    String value();
    /** 逻辑：AND（默认）/ OR */
    Logical logical() default Logical.AND;

    enum Logical { AND, OR }
}
```

`trigger/aspect/RequirePermissionAspect.java`：

```java
package cn.bugstack.trigger.aspect;

import cn.bugstack.trigger.interceptor.UserContext;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验切面：方法/类上声明 @RequirePermission，AOP 校验当前用户是否持有该权限。
 * 把 OKR 原 PermissionService 编程式 if 升级为声明式注解，鉴权与业务解耦。
 */
@Aspect
@Component
public class RequirePermissionAspect {

    @Around("@annotation(rp) || @within(rp)")
    public Object around(ProceedingJoinPoint pjp, RequirePermission rp) throws Throwable {
        Set<String> owned = UserContext.getPermissions();
        if (UserContext.get() != null && UserContext.get().isAdmin()) {
            return pjp.proceed();  // admin 放行
        }
        if (!owned.contains(rp.value())) {
            throw new AppException(ResponseCode.FORBIDDEN.getCode(), "无权限: " + rp.value());
        }
        return pjp.proceed();
    }
}
```

Controller 用法：

```java
@RestController
@RequestMapping("/api/objectives")
@RequiredArgsConstructor
public class OkrObjectiveController implements IOkrObjectiveService {

    private final ObjectiveApplicationService objectiveAppService;

    @PostMapping
    @RequirePermission("okr:objective:create")    // 声明式鉴权
    public Response<Long> create(@RequestBody @Valid ObjectiveCreateDTO dto) {
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(objectiveAppService.create(dto))
                .build();
    }
}
```

### 5.5.5 权限领域服务 + 数据权限（domain 层）

`domain/iam/service/IPermissionService.java`（保留 OKR 原 `PermissionService` 语义，扩展数据权限）：

```java
package cn.bugstack.domain.iam.service;

import cn.bugstack.domain.iam.model.valobj.DataScopeVO;
import java.util.Set;

/**
 * 权限领域服务。
 * 保留 OKR 原 PermissionService 语义（isAdmin/canManageDept/...），并扩展 data_scope 数据权限。
 * 实现在 infrastructure，查五表。
 */
public interface IPermissionService {

    boolean isAdmin();
    boolean isSelf(Long userId);
    boolean isDeptLeader();

    /** 当前用户的数据范围 */
    DataScopeVO currentUserDataScope();

    /** 当前用户可管理的部门ID集合（admin=全部，dept_and_below=本部门及下级，dept=本部门，self=自身） */
    Set<Long> getCurrentUserManageableDepartmentIds();

    /** 是否有权管理目标部门 */
    boolean canManageDept(Long departmentId);

    /** 是否有权查看目标用户 */
    boolean canViewUser(Long targetUserId);
}
```

case 层查询时注入 `IPermissionService` 做数据过滤：

```java
// ObjectiveApplicationService.queryPage
Set<Long> deptIds = permissionService.getCurrentUserManageableDepartmentIds();
if (deptIds != null && !permissionService.isAdmin()) {
    // 非全部数据范围，按部门过滤
    return objectiveRepository.queryPageByDeptIds(page, deptIds);
}
return objectiveRepository.queryPage(page);  // data_scope=all
```

### 5.5.6 JWT 端口反转（domain 接口 + infrastructure 实现）

`domain/iam/adapter/gateway/IJwtGateway.java`：

```java
package cn.bugstack.domain.iam.adapter.gateway;

import cn.bugstack.domain.iam.model.valobj.LoginUserVO;

/**
 * JWT 网关端口（领域接口）。
 * domain 只定义契约，不耦合 jjwt；实现在 infrastructure。
 */
public interface IJwtGateway {
    /** 签发 token */
    String sign(LoginUserVO user);
    /** 校验 token，返回登录用户（含 roles/permissions）；失败返回 null */
    LoginUserVO verify(String token);
}
```

`infrastructure/adapter/gateway/JwtGateway.java`（实现，耦合 jjwt）：

```java
package cn.bugstack.infrastructure.adapter.gateway;

import cn.bugstack.domain.iam.adapter.gateway.IJwtGateway;
import cn.bugstack.domain.iam.model.valobj.LoginUserVO;
import cn.bugstack.infrastructure.dao.ISysUserDao;
import cn.bugstack.infrastructure.dao.ISysUserRoleDao;
import cn.bugstack.infrastructure.dao.ISysRolePermissionDao;
import org.springframework.stereotype.Component;

/**
 * JWT 网关实现：用 jjwt 签发/解析 token；verify 时根据 userId 查五表组装权限集。
 * 仅本类耦合 jjwt 与 DAO，domain 保持纯净（端口反转）。
 */
@Component
public class JwtGateway implements IJwtGateway {

    private final ISysUserDao sysUserDao;
    private final ISysUserRoleDao sysUserRoleDao;
    private final ISysRolePermissionDao sysRolePermissionDao;
    // 构造注入略

    @Override
    public String sign(LoginUserVO user) {
        // jjwt 构建 token，claim 放 userId/account/departmentId
        return null; // 实现略
    }

    @Override
    public LoginUserVO verify(String token) {
        // 1. jjwt 解析 token → userId（过期/非法返回 null）
        // 2. 查 sys_user_role → role_code 集合
        // 3. 查 sys_role_permission → perm_code 集合
        // 4. 组装 LoginUserVO 返回
        return null; // 实现略
    }
}
```

## 5.6 权限种子数据

迁移时在 `data.sql` 初始化（对齐 OKR 原 `admin/admin123` 种子）：

```sql
-- 角色
INSERT INTO sys_role(role_code, role_name, data_scope, status) VALUES
  ('admin', '系统管理员', 'all', 1),
  ('dept_admin', '部门管理员', 'dept_and_below', 1),
  ('user', '普通用户', 'self', 1);

-- 权限点（部分，完整见附录 C）
INSERT INTO sys_permission(perm_code, perm_name, perm_type, path) VALUES
  ('okr:objective:create', '创建目标', 'api', '/api/objectives'),
  ('okr:objective:read',   '查看目标', 'api', '/api/objectives'),
  ('okr:objective:update', '更新目标', 'api', '/api/objectives'),
  ('okr:objective:delete', '删除目标', 'api', '/api/objectives'),
  ('okr:objective:publish', '发布目标', 'api', '/api/objectives/*/publish'),
  ('iam:user:manage', '用户管理', 'api', '/api/users');

-- 角色-权限（admin 拥有全部）
INSERT INTO sys_role_permission(role_id, permission_id)
  SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code='admin';

-- 用户（admin/admin123，密码 BCrypt）
INSERT INTO sys_user(username, account, password, role, status) VALUES
  ('管理员', 'admin', '$2a$10$xxxxBCryptHash', 'admin', 1);
INSERT INTO sys_user_role(user_id, role_id)
  SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.account='admin' AND r.role_code='admin';
```

> `sys_user.role` 字段保留（向后兼容 OKR 原逻辑），但新逻辑以 `sys_user_role` 关联表为准。
