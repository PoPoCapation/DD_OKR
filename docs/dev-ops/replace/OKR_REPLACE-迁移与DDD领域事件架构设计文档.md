# OKR_REPLACE 迁移与 DDD 领域事件架构设计文档

> 把 `D:\project\OKR`（Spring Cloud 微服务、贫血模型、RabbitMQ 事件驱动、半启用 RBAC）迁移为 `D:\project\OKR_REPLACE`（单体 DDD 七模块分层、领域事件驱动、完整 RBAC），分层写法参考 `D:\project\MCP\mcp\ai-mcp-gateway`。

| 项 | 值 |
| --- | --- |
| 文档版本 | v1.0 |
| 编写日期 | 2026-07-05 |
| 源项目 | `D:\project\OKR`（9 个微服务，Spring Boot 3.4.1 + Spring Cloud + RabbitMQ） |
| 参考项目 | `D:\project\MCP\mcp\ai-mcp-gateway`（七模块 DDD，端口反转写法） |
| 目标项目 | `D:\project\OKR_REPLACE`（单体 DDD，Spring Boot 3.4.3 + Java 17） |
| 状态 | 待评审 / 落地实施中 |

---

## 目录

- [0. 一图看懂（TL;DR）](#0-一图看懂tldr)
- [1. 迁移背景与目标](#1-迁移背景与目标)
- [2. 三方项目对齐分析](#2-三方项目对齐分析)
- [3. 目标架构总览](#3-目标架构总览)
- [4. 领域事件驱动设计模式（核心）](#4-领域事件驱动设计模式核心)
- [5. RBAC 权限控制设计（核心）](#5-rbac-权限控制设计核心)
- [6. 分层落地详解（逐模块怎么写）](#6-分层落地详解逐模块怎么写)
- [7. 完整迁移路径（阶段化执行）](#7-完整迁移路径阶段化执行)
- [8. 数据迁移](#8-数据迁移)
- [9. 配置与部署](#9-配置与部署)
- [10. 风险清单与遗留技术债](#10-风险清单与遗留技术债)
- [附录 A：完整目录树](#附录-a完整目录树)
- [附录 B：领域事件清单](#附录-b领域事件清单)
- [附录 C：权限码清单](#附录-c权限码清单)
- [附录 D：API 迁移映射表](#附录-d-api-迁移映射表)

---

## 0. 一图看懂（TL;DR）

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        OKR_REPLACE 单体 DDD                              │
│                                                                          │
│   trigger  ──>  case  ──>  domain  ──>  infrastructure                    │
│   (HTTP/       (用例      (聚合根/       (DAO/PO/Redis/                  │
│    拦截器/      编排)      领域服务/      Repository实现/                 │
│    事件监听)               仓储接口/      JWT工具)                        │
│                           领域事件)                                      │
│       │            │          │              │                           │
│       │            │          │  端口反转    │                           │
│       │            │          │<─────────────│  implements IRepository   │
│       │            │          │              │                           │
│   @RequirePermission   事务边界     publish(Event) ──┐                   │
│   JwtAuthInterceptor   @Transactional                │ @Transactional    │
│                        调领域服务                     │ EventListener      │
│                                                       ▼                   │
│                                          领域事件链路（进程内）          │
│   Task变更 → TaskChangedEvent → 重算KR → KrChangedEvent                │
│            → 重算O → ProgressChangedEvent → 落进度流水                  │
│   任意写  → OperationLogEvent(AFTER_COMMIT,@Async) → 落审计            │
│   CheckIn → CheckInSubmittedEvent → 应用KR申报                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**三句话总结：**

1. **架构形态**：从 OKR 的"9 微服务 + RabbitMQ 跨进程事件"收敛为"单体 DDD + Spring 进程内领域事件"，跨服务 Feign 调用变成同进程方法调用，RabbitMQ 事件变成 `ApplicationEvent`（保留 MQ 作为未来拆分时的可选出口）。
2. **写法借鉴**：抄 ai-mcp-gateway 的**七模块分层 + 依赖方向 + Repository 端口反转 + `IXxxService`/`XxxService` 领域服务组织**；但它**没有实现领域事件**（`trigger/listener/` 是空目录，只有 `package-info` 注释提示方向），事件驱动部分需要我们基于 OKR 原有链路在 DDD 下重新落地。
3. **新增能力**：把 OKR"建了表但代码没启用"的 RBAC 五表（`sys_role`/`sys_permission`/`sys_user_role`/`sys_role_permission` + `data_scope`）完整启用，做成"认证(JWT) + 鉴权(perm_code 注解) + 数据权限(data_scope)"三层，鉴权放 trigger 层拦截器，不污染业务。

---

## 1. 迁移背景与目标

### 1.1 为什么要迁移（OKR 现状痛点）

经对 `D:\project\OKR` 全量摸底，源项目存在以下结构性问题：

| 痛点 | 具体表现 | 影响 |
| --- | --- | --- |
| **贫血模型** | 9 个服务均为 `controller/service/dao` 三层，Entity 仅服务持久化，无行为 | 业务逻辑散落在 Service，领域知识无处沉淀，难以演进 |
| **微服务过度拆分** | 单组织内部系统拆成 9 个服务（gateway/iam/okr-core/task/checkin/progress/operation-log/derivation-worker/common） | 跨服务 Feign 调用 + 共享物理库（`okr_backend`），分布式收益为 0，却背了运维与一致性成本 |
| **事件链路脆弱** | `checkin-service` 的 MQ 发送未走事务后发送（见 `docs/architect/README.md`），存在幻读；RabbitMQ 未编排进 docker-compose | 进度推导可能丢事件/重算错乱 |
| **RBAC 半启用** | RBAC 五表已建（`schema.sql:76-132`），但代码层只靠 `sys_user.role` 字符串 + `PermissionService` 编程式判断，权限点未沉淀 | 权限无法统一治理，新增接口靠手写 `if` |
| **接口命名不规范** | `/api/KR/create/{ObjectiveId}`、`/api/task/addTask`，用 `@RequestMapping` 未限定 HTTP 方法 | 技术债集中，RESTful 语义混乱 |
| **JWT 安全坑** | `iam-service` 默认过期时间 3153600000 秒（≈100 年），secret 为占位值 | 生产前必改 |
| **对齐功能是占位** | `OkrAlignmentController` 仅 `GET /okr-alignment` 占位，无真实逻辑 | 需重新实现 |
| **无迁移工具** | 数据库用 `schema.sql`/`data.sql` 幂等模式，无 Flyway/Liquibase | 表结构演进无版本管理 |

### 1.2 迁移目标

1. **DDD 分层**：采用 ai-mcp-gateway 的七模块分层（types/domain/api/case/infrastructure/trigger/app），领域层充血（聚合根 + 领域服务 + 值对象），业务知识沉淀回领域层。
2. **领域事件驱动**：用 Spring `ApplicationEvent` 把 OKR 原有的 `Task→KR→O→Progress` 推导链路与 `OperationLog` 审计链路在进程内重构为领域事件模式，解耦聚合、解决事务后发送问题。
3. **完整 RBAC**：启用五表 + `data_scope`，做成"认证 + 鉴权 + 数据权限"三层，鉴权声明式（注解），不嵌业务。
4. **单体收敛**：9 微服务 → 1 单体，跨服务调用变进程内调用，降低运维与一致性成本。
5. **保留资产**：复用 OKR_REPLACE 已生成的 17 个 DAO/PO/mapper XML + 16 表 schema，不推倒重来。

### 1.3 迁移边界

| 类别 | 迁移 | 不迁移 / 重新设计 |
| --- | --- | --- |
| 业务领域 | O / KR / Task / Cycle / Alignment / CheckIn / Progress / OperationLog / User / Department | Comment、Review（OKR 原本就不存在，本次不新建） |
| 事件 | `TaskChangedEvent` / `KrChangedEvent` / `ProgressChangedEvent` / `OperationLogEvent` / `CheckInSubmittedEvent` 语义全部保留 | 传输机制从 RabbitMQ 改为 Spring `ApplicationEvent` |
| 权限 | `PermissionService` 接口语义保留 | 实现从"role 字符串"升级为"五表 + data_scope" |
| 数据库 | 16 张表结构（OKR_REPLACE schema.sql 已就绪） | 物理库从 `okr_backend` 改为 `okr_replace` |
| 接口 | 业务能力对齐 | 接口路径统一 RESTful 化（见附录 D） |
| 基础设施 | MySQL / Redis | RabbitMQ 降级为可选（默认不依赖） |

---

## 2. 三方项目对齐分析

### 2.1 源项目 OKR 摸底

```
D:\project\OKR
├── backend/                         # 9 个 Maven 微服务模块
│   ├── okr-common/                  # 共享：BaseEntity、PermissionService、事件 record、Result
│   ├── okr-gateway/                 # Spring Cloud Gateway，GatewayAuthFilter 统一认证
│   ├── iam-service/                 # 用户/部门/角色 + JWT 签发 + /internal/auth/validate
│   ├── okr-core-service/            # Cycle / Objective / KR / Alignment
│   ├── task-service/                # Task / TaskUser / Count
│   ├── checkin-service/             # CheckIn / CheckInItem
│   ├── progress-service/            # ProgressRecord 进度流水
│   ├── operation-log-service/       # OperationLog 审计
│   └── derivation-worker/           # RabbitMQ 消费者，重算 KR/O 进度
├── docs/                            # 大量设计文档（功能/联动/边界/架构师视角）
└── docker-compose.yml               # MySQL + Redis（RabbitMQ 未编排）
```

**技术栈**：Spring Boot 3.4.1 + Spring Cloud 2024.0.0 + MyBatis Plus 3.5.8 + MySQL 8.4 + Redis 7.4 + Redisson + RabbitMQ + JWT(jjwt 0.12.6) + Knife4j。

**事件定义**（`okr-common/src/main/java/com/example/project/common/event/`，全部 `record` + `Serializable`）：

```java
// 任务变更 → 触发 KR 重算
public record TaskChangedEvent(Long krId) implements Serializable {}

// KR 变更 → 触发 O 重算
public record KrChangedEvent(Long objectiveId) implements Serializable {}

// 操作审计（业务服务只发，operation-log-service 落库）
public record OperationLogEvent(
    String serviceName, String resourceType, Long resourceId, String action,
    Long operatorId, String beforeJson, String afterJson,
    String requestId, String ip
) implements Serializable {}
// 另有 CheckInSubmittedEvent、ProgressChangedEvent
```

**事件链路**（`docs/OKR系统联动设计文档.md`）：

```
Task 写操作 ──发送 TaskChangedEvent──> derivation-worker
                                         │ recalculateKrProgress(krId)
                                         ▼
                                    更新 KR completion_rate
                                         │ 发送 KrChangedEvent
                                         ▼
                                    recalculateObjectiveProgress(objectiveId)
                                         │ 更新 O progress
                                         ▼
                                    发送 ProgressChangedEvent → progress-service 落流水

任意业务写 ──发送 OperationLogEvent──> operation-log-service 落 okr_operation_log

CheckIn 提交 ──发送 CheckInSubmittedEvent──> derivation-worker 应用 KR 申报结果
```

**权限现状**：`PermissionService`（`okr-common/.../common/auth/PermissionService.java`）提供 `isAdmin()`/`isSelf(userId)`/`isDeptLeader()`/`canViewUser(...)`/`canManageDept(deptId)`/`getCurrentUserManageableDepartmentIds()` 等编程式方法；网关 `GatewayAuthFilter` 校验 JWT 后透传 `X-User-Id`/`X-Roles`/`X-Permissions` 头；RBAC 五表已建但代码未启用。

### 2.2 参考项目 ai-mcp-gateway 写法结构

```
ai-mcp-gateway/   (cn.bugstack.ai.*)
├── ai-mcp-gateway-types/        # ResponseCode / AppException / Constants
├── ai-mcp-gateway-api/          # IMcpGatewayService 接口 + Response<T>
├── ai-mcp-gateway-domain/       # 领域核心
│   └── auth|session|protocol/   # 按子域组织
│       ├── model/{entity,valobj,enums}   # 命令对象 + 值对象（无聚合根实现）
│       ├── adapter/repository/IXxxRepository.java   # 仓储接口（端口）
│       ├── adapter/port/IXxxPort.java               # 出站端口
│       └── service/{IXxxService, XxxService}        # 领域服务
├── ai-mcp-gateway-case/         # 用例编排（策略树框架 xfg-wrench-starter-design-framework）
├── ai-mcp-gateway-infrastructure/  # Repository/Port 实现 + MyBatis DAO/PO + Retrofit 网关
├── ai-mcp-gateway-trigger/      # http(Controller) / job(空) / listener(空)
└── ai-mcp-gateway-app/          # Application 启动类 + config
```

**依赖方向**：`types → domain → {api, case, infrastructure} → trigger → app`

**值得抄的 4 个写法**：

1. **端口反转**：Repository 接口定义在 `domain/adapter/repository/`，实现在 `infrastructure/adapter/repository/`，domain 不依赖 infrastructure（依赖倒置）。例：
   - 接口 `IAuthRepository`（`ai-mcp-gateway-domain/.../auth/adapter/repository/IAuthRepository.java`）
   - 实现 `AuthRepository implements IAuthRepository`（`ai-mcp-gateway-infrastructure/.../adapter/repository/AuthRepository.java`），内部注入 `IMcpGatewayAuthDao`（MyBatis `@Mapper`），把 `PO` 手工组装成领域 `VO` 返回。
2. **领域服务组织**：每个子域一个 `IXxxService` 接口 + `XxxService` 实现类（`@Service`）。
3. **types/api 基础类**：`Response<T>`（code/info/data，`@Builder`）、`ResponseCode` 枚举、`AppException` 运行时异常。
4. **七模块依赖方向清晰**，trigger 只做协议接入，业务编排下沉到 case，单点逻辑在 domain。

**必须补的 5 个缺口**（ai-mcp-gateway 没有，迁移时要自己实现）：

| 缺口 | ai-mcp-gateway 现状 | 本次需做 |
| --- | --- | --- |
| **领域事件** | `trigger/listener/package-info.java` 注释："类似MQ的使用，如Spring的Event，Guava的事件总线都可以"——**空目录，无实现** | 第四章完整设计：Spring `ApplicationEvent` + `@TransactionalEventListener` |
| **聚合根** | `model/aggregate/` 全是空 `package-info`，用命令对象 Entity | 第六章：为 O/KR/Task 等补充充血聚合根 |
| **事务边界** | 生产代码无 `@Transactional` | case 层应用服务加 `@Transactional` |
| **RBAC/拦截器** | 只有 apiKey 校验，嵌在 case 策略树节点里；无 Filter/Interceptor/`@PreAuthorize` | 第五章：trigger 层拦截器 + 注解 AOP + 五表 RBAC |
| **Redis** | `infrastructure/redis/` 空目录，缓存散落在 app/domain | 第九章：统一接通 Redis，放 infrastructure |

### 2.3 目标项目 OKR_REPLACE 现状

```
D:\project\OKR_REPLACE   (cn.bugstack.*)
├── pom.xml              # ⚠️ 第 17-18 行 OKR_REPLACE-case 重复声明
├── OKR_REPLACE-types/        # ✅ 已有 Constants / ResponseCode / AppException（toString 残留旧包名）
├── OKR_REPLACE-api/          # ✅ 已有 Response<T>；dto/api 仅 package-info
├── OKR_REPLACE-domain/       # ⚠️ 纯空壳，脚手架 xxx/yyy 示例需替换
├── OKR_REPLACE-case/         # ⚠️ 纯空壳 + pom 自循环依赖（case 依赖自身）
├── OKR_REPLACE-infrastructure/  # ✅✅ 核心资产：17 个 DAO + 17 个 PO + 17 个 mapper XML
├── OKR_REPLACE-trigger/      # ⚠️ http/job/listener 三个空 package-info
├── OKR_REPLACE-app/          # ✅ Application 启动类 + Guava/ThreadPool 配置；application-dev.yml（库名仍为 xfg_frame_archetype）
└── docs/dev-ops/
    ├── docker-compose-*.yml  # MySQL(13306) + Redis(16379) + phpmyadmin
    ├── mysql/sql/schema.sql  # ✅ 16 张表已就绪
    └── replace/              # ⬅ 本文档所在目录（迁移专属部署位）
```

**已有资产清单（直接复用，不重写）**：

- `infrastructure/dao/`：17 个 `@Mapper` 接口（`IOkrObjectiveDao` / `IOkrKeyResultDao` / `IOkrTaskDao` / `IOkrCycleDao` / `IOkrCheckInDao` / `IOkrProgressRecordDao` / `IOkrOperationLogDao` / `ISysUserDao` / `ISysRoleDao` / `ISysPermissionDao` / `ISysUserRoleDao` / `ISysRolePermissionDao` / `ISysDepartmentDao` / `IOkrObjectiveUserDao` / `IOkrTaskUserDao` / `IOkrObjectiveAlignmentDao` / `IOkrCheckInItemDao`）。
- `infrastructure/dao/po/`：17 个 PO（如 `OkrObjectivePO`：`id/objectiveName/ownerUserId/departmentId/cycleId/progress/status/remark/isDeleted/createtime/updatetime`）。
- `app/src/main/resources/mybatis/mapper/*.xml`：17 个 mapper XML（如 `okr_objective_mapper.xml` 已含 `resultMap` + 动态 `insert/update/delete` + 逻辑删除 `is_deleted=0`）。
- `docs/dev-ops/mysql/sql/schema.sql`：16 张表（系统表 6：`sys_user/sys_department/sys_role/sys_permission/sys_user_role/sys_role_permission`；业务表 10：`okr_objective/okr_key_result/okr_task/okr_objective_user/okr_task_user/okr_cycle/okr_objective_alignment/okr_progress_record/okr_check_in/okr_check_in_item/okr_operation_log`），带幂等 ALTER 处理。

**待修复问题清单（迁移前必修）**：

| 级别 | 问题 | 位置 | 修复 |
| --- | --- | --- | --- |
| 🔴 阻断 | `OKR_REPLACE-case` 在根 pom 重复声明两次 | `pom.xml:17-18` | 删除重复行 |
| 🔴 阻断 | case 模块 pom 依赖自身（自循环） | `OKR_REPLACE-case/pom.xml` | 删自依赖 |
| 🟡 配置 | dev 库名 `xfg_frame_archetype` | `application-dev.yml:20` | 改 `okr_replace` |
| 🟡 配置 | test/prod 数据源与 mybatis 整段注释 | `application-test.yml`/`-prod.yml` | 按环境补齐 |
| 🟡 缺失 | Redis 完全没接通（无依赖、无配置、无代码） | 全局 | 第九章接通 |
| 🟢 清理 | `AppException.toString()` 残留 `cn.bugstack.x.api.types.exception.XApiException` | `AppException.java:40` | 改 `cn.bugstack.types.exception.AppException` |
| 🟢 清理 | case 包名 `cn.bugstack.ai.cases` 与他模块不一致 | `OKR_REPLACE-case` | 改 `cn.bugstack.case` |
| 🟢 清理 | prod profile `HeapDumpPath` 残留 `fq-mall-activity-app` | `pom.xml:205` | 改 `OKR_REPLACE` |
| 🟢 清理 | domain 下脚手架 xxx/yyy 示例 | `OKR_REPLACE-domain` | 替换为真实 OKR 子域 |

### 2.4 关键差异与设计取舍

| 维度 | OKR（源） | ai-mcp-gateway（参考） | OKR_REPLACE（目标） | 取舍理由 |
| --- | --- | --- | --- | --- |
| 部署形态 | 9 微服务 | 单体 | **单体** | 单组织内部系统，分布式收益 < 成本 |
| 分层 | 贫血 controller/service/dao | DDD 七模块 | **DDD 七模块** | 抄 ai-mcp-gateway |
| 事件机制 | RabbitMQ 跨进程 | 无实现（空 listener） | **Spring ApplicationEvent 进程内** | 单体内进程内事件最轻；保留 MQ 作可选出口 |
| 事件发送时机 | 部分未走事务后（幻读） | 无 | **`@TransactionalEventListener(AFTER_COMMIT)`** | 解决 OKR 幻读坑 |
| 鉴权位置 | 网关 Filter + Service 内 if | 嵌在 case 策略节点 | **trigger 拦截器 + 注解 AOP** | 鉴权与业务解耦 |
| RBAC | 五表建了没启用 | 无 | **完整启用五表 + data_scope** | 补齐 OKR 遗留 |
| 持久化 | MyBatis Plus | MyBatis（XML） | **MyBatis（XML，复用已有）** | 复用 17 个 mapper |
| 聚合根 | 无 | 无（命令对象） | **充血聚合根** | DDD 正道，行为内聚 |

## 3. 目标架构总览

### 3.1 七模块分层与依赖方向

```
                    ┌──────────────┐
                    │     app      │  启动 + 配置 + 资源文件
                    └──────┬───────┘
                           │ 依赖
              ┌────────────┴────────────┐
              ▼                         ▼
        ┌──────────┐            ┌──────────────────┐
        │ trigger  │            │ infrastructure   │
        │ HTTP/Job │            │ DAO/PO/Redis/JWT │
        │ Listener │            │ Repository实现   │
        └────┬─────┘            └────────┬─────────┘
             │ 依赖                       │ implements
   ┌─────────┼─────────────┐             │
   ▼         ▼             ▼             │
┌──────┐ ┌──────┐  ┌──────────┐          │
│ api  │ │ case │  │  domain  │◄─────────┘  端口反转：domain 定义 IRepository 接口
│DTO/  │ │用例  │  │ 聚合根/  │             infrastructure 实现它
│Resp  │ │编排  │  │ 领域服务/│
│契约  │ │@Tx   │  │ 仓储接口/│
└──┬───┘ │发事件│  │ 领域事件 │
   │     └──┬───┘  └────┬─────┘
   │        │           │ 依赖
   │        └──────────►│  case 依赖 domain
   │                    │
   └────────────────────►│  api 被 trigger 实现；api 也可被 domain/case 引用作 DTO
                         │
                    ┌────┴─────┐
                    │  types   │  枚举/异常/常量（最底层，无业务依赖）
                    └──────────┘
```

**依赖铁律**（DDD 核心）：

- `types` 不依赖任何工程模块。
- `domain` 只依赖 `types`（**绝不依赖 infrastructure**，靠端口反转）。
- `infrastructure` 依赖 `domain`（实现 domain 的接口）。
- `case` 依赖 `domain`（编排领域服务）。
- `trigger` 依赖 `case` + `api` + `domain` + `types`。
- `app` 依赖 `trigger` + `infrastructure`（聚合启动）。
- `api` 独立，只依赖 `types`（契约层）。

### 3.2 各模块职责

| 模块 | 职责 | 关键内容 |
| --- | --- | --- |
| **types** | 通用类型，无业务 | `ResponseCode`、`AppException`、`Constants`、通用枚举（`DataScopeEnum`、`OkrStatusEnum` 等） |
| **api** | 对外契约 | `IOkrObjectiveService` 等接口、`Response<T>`、DTO/VO（请求/响应对象） |
| **domain** | 领域核心 | 聚合根、实体、值对象、领域服务、Repository/Port 接口、**领域事件定义** |
| **case** | 用例编排 | ApplicationService：事务边界 `@Transactional`、调领域服务、**发布领域事件**、DTO↔领域对象转换 |
| **infrastructure** | 落地实现 | Repository/Port 实现、MyBatis DAO/PO/mapper、Redis、JWT 工具、外部网关 |
| **trigger** | 入口适配 | HTTP Controller、拦截器（鉴权）、`@Scheduled` 定时任务、**领域事件监听器** |
| **app** | 启动配置 | `Application`、`@Configuration`、`application*.yml`、mapper XML、logback |

### 3.3 包结构约定

统一根包 `cn.bugstack`，各模块子包：

```
cn.bugstack.types            # types 模块
  ├── common/Constants
  ├── enums/{ResponseCode, DataScopeEnum, OkrStatusEnum, KrStatusEnum, TaskStatusEnum, CycleStatusEnum, ...}
  └── exception/AppException

cn.bugstack.api              # api 模块
  ├── IOkrObjectiveService, IOkrKeyResultService, IOkrTaskService, ...   # 接口契约
  ├── dto/            # 请求 DTO（创建/更新/查询条件）
  ├── vo/             # 响应 VO
  └── response/Response<T>

cn.bugstack.domain           # domain 模块（按子域组织）
  ├── iam/                  # 用户/部门/角色/权限（身份与访问）
  │   ├── model/{aggregate, entity, valobj}
  │   ├── service/{IIamService, IPermissionService, ...}
  │   ├── adapter/repository/IIamRepository
  │   └── adapter/gateway/IJwtGateway        # JWT 端口
  ├── cycle/                # OKR 周期
  ├── objective/            # Objective 聚合（核心）
  │   ├── model/{aggregate/ObjectiveAggregate, entity, valobj}
  │   ├── service/{IObjectiveService, ObjectiveService}
  │   ├── adapter/repository/IObjectiveRepository
  │   └── model/event/{ObjectiveProgressChangedEvent, ObjectiveStatusChangedEvent}
  ├── keyresult/            # KR
  ├── task/                 # Task
  │   └── model/event/TaskChangedEvent
  ├── alignment/            # 目标对齐
  ├── checkin/              # Check-in
  ├── progress/             # 进度流水
  ├── operationlog/         # 操作审计
  │   └── model/event/OperationLogEvent
  ├── derivation/           # 进度推导（领域服务，被事件监听器调用）
  │   └── service/{IOkrDerivationService, OkrDerivationService}
  └── event/                # 跨子域共享的事件基类
      └── AbstractDomainEvent

cn.bugstack.case             # case 模块
  ├── objective/{ObjectiveApplicationService}
  ├── keyresult/{KeyResultApplicationService}
  ├── task/{TaskApplicationService}
  ├── cycle/{CycleApplicationService}
  ├── checkin/{CheckInApplicationService}
  └── iam/{IamApplicationService}

cn.bugstack.infrastructure   # infrastructure 模块
  ├── adapter/repository/{ObjectiveRepository, KeyResultRepository, ...}  # 实现 domain 接口
  ├── adapter/gateway/{JwtGateway}                # 实现 IJwtGateway
  ├── dao/{IOkrObjectiveDao, ...}                 # ✅ 已有 17 个
  ├── dao/po/{OkrObjectivePO, ...}                # ✅ 已有 17 个
  └── redis/{IRedisService, RedisService}         # 待补

cn.bugstack.trigger          # trigger 模块
  ├── http/{OkrObjectiveController, OkrKeyResultController, ...}   # Controller
  ├── interceptor/{JwtAuthInterceptor, UserContext}   # JWT 认证拦截器 + 用户上下文
  ├── aspect/{RequirePermission, RequirePermissionAspect}  # 鉴权注解 + AOP
  ├── job/{ProgressRecalculateJob}                # 定时任务（可选）
  └── listener/                                   # ✅ 领域事件监听器（本次补实现）
      ├── TaskChangedEventListener
      ├── KrChangedEventListener
      ├── ProgressChangedEventListener
      ├── CheckInSubmittedEventListener
      └── OperationLogEventListener

cn.bugstack                  # app 模块
  ├── Application
  ├── config/{WebMvcConfig, RedisConfig, MybatisConfig, JwtConfig}
  └── (resources: application*.yml, mybatis/, logback-spring.xml)
```

### 3.4 领域子域划分（限界上下文）

| 子域 | 限界上下文 | 聚合根 | 关联表 |
| --- | --- | --- | --- |
| **iam** | 身份与访问管理 | User、Department、Role | `sys_user`、`sys_department`、`sys_role`、`sys_user_role` |
| **iam.permission** | 权限点管理 | Permission | `sys_permission`、`sys_role_permission` |
| **cycle** | OKR 周期 | Cycle | `okr_cycle` |
| **objective** | 目标（O） | Objective（聚合 KR 列表） | `okr_objective`、`okr_objective_user` |
| **keyresult** | 关键结果（KR） | KeyResult | `okr_key_result` |
| **task** | 任务 | Task（聚合执行人） | `okr_task`、`okr_task_user` |
| **alignment** | 目标对齐 | Alignment | `okr_objective_alignment` |
| **checkin** | Check-in | CheckIn（聚合明细） | `okr_check_in`、`okr_check_in_item` |
| **progress** | 进度流水 | ProgressRecord（只追加） | `okr_progress_record` |
| **operationlog** | 操作审计 | OperationLog（只追加） | `okr_operation_log` |
| **derivation** | 进度推导（领域服务，无独立聚合） | — | 复用 keyresult/objective/task 仓储 |

---

## 4. 领域事件驱动设计模式（核心）

### 4.1 为什么用领域事件

OKR 的核心联动是 **O → KR → Task** 三级进度推导：

- Task 完成率变化 → 重算所属 KR 完成率（`已完成任务数/有效任务数*100`）
- KR 完成率变化 → 重算所属 O 进度（`Σ(权重/100 * KR完成率/100)*100`）
- O/KR/Task 进度变化 → 追加进度流水
- 任意写操作 → 追加操作审计

如果让 TaskApplicationService 直接调 KeyResultService、KeyResultService 直接调 ObjectiveService，会形成**紧耦合的调用链**（Task 依赖 KR、KR 依赖 O），且事务边界混乱。领域事件把这些依赖反转：**变更方只"喊一声"（发事件），关心方自己"订阅"（监听）**，变更方不需要知道谁在听。

> 设计原则：**领域事件是领域语义的一部分，不是基础设施细节**。`TaskChangedEvent` 表达的是"任务的进度/状态变了"这个领域事实，至于它是通过 Spring Event 还是 RabbitMQ 传递，是基础设施的选择。

### 4.2 ai-mcp-gateway 的启示与不足

ai-mcp-gateway 的 `trigger/listener/package-info.java` 注释原文：

```java
/**
 * 监听服务；在单体服务中，解耦流程。类似MQ的使用，如Spring的Event，Guava的事件总线都可以。
 * 如果使用了 Redis 那么也可以有发布/订阅使用。
 * Guava：https://bugstack.cn/md/road-map/guava.html
 */
package cn.bugstack.ai.trigger.listener;
```

**启示**：它指明了三件事——(1) 监听器放 `trigger/listener` 层；(2) 单体内解耦用 Spring Event / Guava EventBus / Redis Pub-Sub 都行；(3) 本质是"类似 MQ 的使用"。

**不足**：它**只给了注释，没给实现**。本次迁移要补完整实现，并做出明确选型。

### 4.3 事件分层归属（关键设计）

| 关注点 | 归属层 | 说明 |
| --- | --- | --- |
| 事件**定义**（`XxxEvent` record/class） | `domain/<子域>/model/event/` | 事件是领域语义，归 domain；跨子域共享基类放 `domain/event/` |
| 事件**发布** | `case`（应用服务）或 `domain`（聚合根/领域服务） | 聚合根行为内发事件最 DDD；本次为降低复杂度，统一在 case 层应用服务发 |
| 事件**监听**（`@EventListener`） | `trigger/listener/` | 抄 ai-mcp-gateway 的 listener 目录位置；监听器是入口适配，调 case/domain 完成业务 |
| 监听器调用的**推导逻辑** | `domain/derivation/service/` | 推导是领域逻辑，归 domain；listener 只做"收到事件 → 调推导服务"的胶水 |

> 为什么监听器放 trigger 而非 domain？因为监听器依赖 Spring 的 `@EventListener` 注解和 `ApplicationEventPublisher`，这些是框架设施；domain 保持纯净（只依赖 types），不耦合 Spring。这与 ai-mcp-gateway 把 listener 放 trigger 的做法一致。

### 4.4 事件机制选型（决策矩阵）

| 方案 | 一致性 | 性能 | 复杂度 | 适用 | 本次选择 |
| --- | --- | --- | --- | --- | --- |
| Spring `@EventListener`（同步） | 强（同事务/同线程） | 中 | 低 | 级联推导需强一致 | ✅ Task→KR→O 推导 |
| Spring `@TransactionalEventListener(AFTER_COMMIT)` | 强（事务提交后才发） | 中 | 低 | 避免幻读、跨事务边界 | ✅ OperationLog 审计、CheckIn 应用 |
| `@Async + @EventListener` | 弱（异步） | 高 | 中 | 不阻塞主流程的副作用 | ✅ OperationLog 审计（事务后异步落库） |
| RabbitMQ | 最终一致 | 高（可跨进程） | 高 | 跨服务/削峰 | ⏸ 可选，默认不依赖（未来拆微服务再启用） |
| Guava EventBus | 弱 | 高 | 低 | 单体内解耦 | ❌ 不选（Spring Event 已够，少引依赖） |

**最终选型**：

- **进程内强一致级联**：`@EventListener`（同步，在 case 层 `@Transactional` 内，推导与主操作同事务回滚）。
- **事务后副作用**：`@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async`（审计、流水等不需要回滚的副作用，事务提交成功后再异步执行，既避免幻读又不阻塞主流程）。
- **RabbitMQ**：保留 OKR 原有 `record` 事件类作为 MQ payload 的备选形态，但在单体阶段默认不启用 MQ，仅当未来拆分微服务时，把 `@EventListener` 换成 `@RabbitListener` 即可，事件定义不变。

### 4.5 事件目录结构

```
OKR_REPLACE-domain/src/main/java/cn/bugstack/domain/
├── event/
│   └── AbstractDomainEvent.java              # 事件基类（eventId/occurredOn/aggregateId）
├── task/model/event/
│   └── TaskChangedEvent.java                 # 任务变更（krId）
├── keyresult/model/event/
│   └── KrChangedEvent.java                   # KR变更（objectiveId）
├── objective/model/event/
│   ├── ObjectiveProgressChangedEvent.java    # O进度变更（落流水用）
│   └── ObjectiveStatusChangedEvent.java
├── checkin/model/event/
│   └── CheckInSubmittedEvent.java            # CheckIn提交（checkInId）
├── progress/model/event/
│   └── ProgressRecordedEvent.java            # 进度已落流水（可选，供下游通知）
└── operationlog/model/event/
    └── OperationLogEvent.java                # 操作审计（serviceName/resourceType/...）

OKR_REPLACE-trigger/src/main/java/cn/bugstack/trigger/listener/
├── TaskChangedEventListener.java             # → 调 derivation 重算 KR
├── KrChangedEventListener.java               # → 调 derivation 重算 O
├── ProgressChangedEventListener.java         # → 落 okr_progress_record
├── CheckInSubmittedEventListener.java        # → 调 derivation 应用 KR 申报
└── OperationLogEventListener.java            # → 落 okr_operation_log（@Async AFTER_COMMIT）
```

### 4.6 完整代码示例

#### 4.6.1 事件基类与领域事件定义（domain 层）

`domain/event/AbstractDomainEvent.java`：

```java
package cn.bugstack.domain.event;

import lombok.Getter;
import java.io.Serializable;

/**
 * 领域事件基类。
 * 所有领域事件携带：事件ID、发生时间、所属聚合ID，便于排查与幂等。
 * 注意：领域事件归 domain 层，不依赖 Spring，仅实现 Serializable。
 */
@Getter
public abstract class AbstractDomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 事件唯一ID（用于消费者幂等） */
    private final String eventId;
    /** 事件发生时间戳 */
    private final Long occurredOn;
    /** 产生事件的聚合根ID */
    private final Long aggregateId;

    protected AbstractDomainEvent(String eventId, Long occurredOn, Long aggregateId) {
        this.eventId = eventId;
        this.occurredOn = occurredOn;
        this.aggregateId = aggregateId;
    }
}
```

`domain/task/model/event/TaskChangedEvent.java`（对齐 OKR 原 `record`，扩展基类字段）：

```java
package cn.bugstack.domain.task.model.event;

import java.io.Serializable;

/**
 * 任务变更领域事件。
 * 当任务的进度或状态发生变化时发布；消费方重算所属 KR 完成率。
 *
 * @param krId 发生变更的任务所属 KR ID（为空表示任务未关联 KR，监听器忽略）
 */
public record TaskChangedEvent(
        String eventId,
        Long occurredOn,
        Long krId
) implements Serializable {

    public static TaskChangedEvent of(Long krId, String eventId, Long occurredOn) {
        return new TaskChangedEvent(eventId, occurredOn, krId);
    }
}
```

> 说明：OKR 原版是 `record TaskChangedEvent(Long krId)`。为支持幂等与排查，扩展 `eventId`/`occurredOn`。若想完全沿用原 record 形态，可去掉扩展字段——但建议保留，事件消费者可基于 `eventId` 做幂等去重。

`domain/operationlog/model/event/OperationLogEvent.java`（对齐 OKR 原版字段）：

```java
package cn.bugstack.domain.operationlog.model.event;

import java.io.Serializable;

/**
 * 操作审计领域事件。
 * 业务服务只负责"谁对什么资源做了什么操作"发出来；监听器落库为审计流水。
 * 字段与 OKR 原 OperationLogEvent 完全对齐，保证迁移语义无损。
 */
public record OperationLogEvent(
        String serviceName,    // okr-core / task / iam ...
        String resourceType,   // OBJECTIVE / KR / TASK / CYCLE / USER / DEPARTMENT
        Long resourceId,
        String action,         // CREATE / UPDATE / DELETE / STATUS_CHANGE / LOGIN
        Long operatorId,       // 系统推导时可为空
        String beforeJson,     // 操作前快照
        String afterJson,      // 操作后快照
        String requestId,      // 链路ID
        String ip
) implements Serializable {}
```

#### 4.6.2 事件发布器（case 层）

`case/task/TaskApplicationService.java`：

```java
package cn.bugstack.case.task;

import cn.bugstack.domain.task.model.aggregate.TaskAggregate;
import cn.bugstack.domain.task.model.event.TaskChangedEvent;
import cn.bugstack.domain.task.adapter.repository.ITaskRepository;
import cn.bugstack.api.dto.TaskCreateDTO;
import cn.bugstack.api.vo.TaskVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Task 用例应用服务。
 * 职责：事务边界 + 编排领域服务 + 发布领域事件 + DTO↔聚合根转换。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskApplicationService {

    private final ITaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 更新任务（含状态/进度变更），事务提交后由监听器推导 KR/O 进度。
     * 推导通过 @TransactionalEventListener 在事务提交后触发，
     * 避免主事务回滚后仍触发推导的幻读问题（修复 OKR 原 checkin 的坑）。
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskVO updateTask(Long taskId, TaskCreateDTO dto) {
        // 1. 加载聚合根
        TaskAggregate task = taskRepository.findById(taskId);
        if (task == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "任务不存在");
        }
        // 2. 聚合根充血行为：变更状态/进度（领域规则内聚在聚合根）
        task.changeStatus(dto.getStatus());
        // 3. 持久化
        taskRepository.save(task);
        // 4. 发布领域事件（AFTER_COMMIT 监听器在事务提交后才消费）
        eventPublisher.publishEvent(TaskChangedEvent.of(
                task.getKrId(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        ));
        return TaskVO.of(task);
    }
}
```

> **为什么发事件在 `@Transactional` 方法内、但监听器用 `AFTER_COMMIT`？**
> `eventPublisher.publishEvent()` 是同步调用，但 `@TransactionalEventListener(AFTER_COMMIT)` 的回调会被 Spring 延迟到当前事务成功提交后才执行。这样：(1) 事件发布与主操作在同一事务上下文，语义清晰；(2) 若主事务回滚，`AFTER_COMMIT` 监听器不执行，避免"任务没更新成功却重算了 KR"的幻读。这正是 OKR 原 checkin 没做对的地方。

#### 4.6.3 事件监听器（trigger 层，本次补实现）

`trigger/listener/TaskChangedEventListener.java`：

```java
package cn.bugstack.trigger.listener;

import cn.bugstack.domain.derivation.service.IOkrDerivationService;
import cn.bugstack.domain.task.model.event.TaskChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 任务变更事件监听器。
 * 收到事件后调用推导领域服务重算 KR 完成率，并级联更新 O 进度。
 *
 * 采用 @EventListener（同步）：与 OKR 原 derivation-worker 语义对齐，
 * 但从跨进程 MQ 改为进程内同步调用，保证推导与主操作的事务一致性。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskChangedEventListener {

    private final IOkrDerivationService derivationService;

    @EventListener
    public void handle(TaskChangedEvent event) {
        if (event.krId() == null) {
            return;  // 任务未关联 KR，无需推导
        }
        log.info("收到任务变更事件，重算 KR 进度，krId={}", event.krId());
        derivationService.recalculateKrProgress(event.krId());
    }
}
```

`trigger/listener/OperationLogEventListener.java`（异步 + 事务后，关键范例）：

```java
package cn.bugstack.trigger.listener;

import cn.bugstack.domain.operationlog.model.event.OperationLogEvent;
import cn.bugstack.domain.operationlog.adapter.repository.IOperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 操作审计事件监听器。
 * 业务写操作只负责发 OperationLogEvent，本监听器落库为审计流水。
 *
 * @TransactionalEventListener(AFTER_COMMIT) + @Async：
 *   - AFTER_COMMIT：主事务提交成功后才执行，避免主操作回滚却留下审计的幻读；
 *   - @Async：异步执行，不阻塞主业务返回。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogEventListener {

    private final IOperationLogRepository operationLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OperationLogEvent event) {
        try {
            operationLogRepository.record(event);
        } catch (Exception e) {
            // 审计失败不影响主业务，仅记录日志
            log.error("操作审计落库失败 event={}", event, e);
        }
    }
}
```

> `@Async` 需要 app 层开启异步支持：`OKR_REPLACE-app` 已有 `ThreadPoolConfig`（`@EnableAsync`），可直接复用其线程池。

### 4.7 OKR 核心事件链路（全链路代码走读）

迁移后，OKR 原有的 `Task → KR → O → Progress` 推导 + `OperationLog` 审计链路在进程内重构为：

```
[trigger/http] TaskController.updateTask
        │
        ▼
[case] TaskApplicationService.updateTask   @Transactional
        │ 1. taskRepository.findById → TaskAggregate
        │ 2. task.changeStatus(...)（聚合根充血）
        │ 3. taskRepository.save
        │ 4. publishEvent(TaskChangedEvent)  ──────────────────┐
        │ 5. publishEvent(OperationLogEvent) ─────────────┐    │
        ▼                                                 │    │
   事务提交 ────────────────────────────────────────────┼────┼──┘
        │                                                 │    │
        ▼ (AFTER_COMMIT)                                  │    ▼ (AFTER_COMMIT, @Async)
[trigger/listener] OperationLogEventListener              │    落 okr_operation_log
                                                          │
                                          [trigger/listener] TaskChangedEventListener
                                                          │
                                                          ▼
                                          [domain/derivation] OkrDerivationService.recalculateKrProgress(krId)
                                                          │  重算 KR completion_rate
                                                          │ publishEvent(KrChangedEvent(objectiveId))
                                                          ▼
                                          [trigger/listener] KrChangedEventListener
                                                          │
                                                          ▼
                                          [domain/derivation] OkrDerivationService.recalculateObjectiveProgress(objectiveId)
                                                          │  重算 O progress
                                                          │ publishEvent(ProgressChangedEvent)
                                                          ▼
                                          [trigger/listener] ProgressChangedEventListener
                                                          │
                                                          ▼
                                          [domain/progress] 落 okr_progress_record
```

> **与 OKR 原版的差异**：原版 `derivation-worker` 是独立微服务，靠 RabbitMQ 跨进程触发；迁移后 `derivation` 是 domain 层的一个领域服务，监听器同进程同步调用，无需 MQ。事件定义（`TaskChangedEvent`/`KrChangedEvent`）语义与字段保持不变，未来若拆微服务，把 `@EventListener` 换成 `@RabbitListener`、把 `eventPublisher.publishEvent` 换成 `rabbitTemplate.convertAndSend` 即可，事件 record 复用。

### 4.8 事件一致性策略

| 事件 | 监听方式 | 一致性 | 失败处理 |
| --- | --- | --- | --- |
| `TaskChangedEvent` | `@EventListener` 同步 | 强一致（与主事务同回滚） | 抛异常回滚主事务 |
| `KrChangedEvent` | `@EventListener` 同步 | 强一致 | 同上 |
| `ProgressChangedEvent` | `@TransactionalEventListener(AFTER_COMMIT)` | 最终一致 | 失败仅日志，可由定时任务补偿 |
| `CheckInSubmittedEvent` | `@TransactionalEventListener(AFTER_COMMIT)` | 最终一致 | 失败仅日志，CheckIn 状态留 `PENDING` 可重试 |
| `OperationLogEvent` | `@Async + AFTER_COMMIT` | 最终一致 | 失败仅日志（审计可丢，不阻塞业务） |

> **幂等**：所有监听器建议基于事件 `eventId` 做幂等（如 `okr_operation_log.request_id` 字段、`okr_progress_record` 可加 `request_id`），防止重试导致重复落库。

## 5. RBAC 权限控制设计（核心）

### 5.1 OKR 现状与目标

| 维度 | OKR 现状 | OKR_REPLACE 目标 |
| --- | --- | --- |
| 表 | 五表已建未启用 | **完整启用**五表 + 种子数据 |
| 认证 | JWT（jjwt），网关统一校验，透传头 | JWT（复用已声明的 jjwt 0.9.1 / java-jwt 4.4.0），trigger 拦截器校验 |
| 鉴权 | `PermissionService` 编程式 `if`，散落各 Service | **声明式** `@RequirePermission("okr:objective:create")` 注解 + AOP |
| 数据权限 | 部分接入 `canManageDept` | **`data_scope`**（all/dept/dept_and_below/self）统一拦截 |
| 用户上下文 | `SecurityContext`(ThreadLocal) + 透传头 | `UserContext`(ThreadLocal)，拦截器从 JWT 填充 |
| 鉴权位置 | 网关 Filter + Service 内 | **trigger 拦截器 + AOP**（不污染 domain/case） |

### 5.2 RBAC 模型

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

### 5.3 三层鉴权

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

### 5.4 鉴权归属层（关键设计）

- **`UserContext`**：放 `trigger/interceptor/`，ThreadLocal 持有当前用户。`LoginUser` 值对象定义在 `domain/iam/model/valobj/LoginUserVO`（保持 domain 纯净，trigger 引用 domain）。
- **`JwtAuthInterceptor`**：`trigger/interceptor/`，解析 JWT 填充 `UserContext`。
- **`@RequirePermission` + `RequirePermissionAspect`**：注解 + 切面均放 `trigger/aspect/`。
- **`IPermissionService`**：接口放 `domain/iam/service/`（领域服务），实现放 `infrastructure`（查五表）。
- **`IJwtGateway`**：JWT 签发/解析端口接口放 `domain/iam/adapter/gateway/`，实现放 `infrastructure/adapter/gateway/`（端口反转，domain 不耦合 jjwt）。

> 为什么鉴权放 trigger 而非 case/domain？鉴权是"接入控制"，属于 trigger 的职责（ai-mcp-gateway 把校验放 case 是因为它没分层鉴权概念，本次改进）。domain/case 保持纯净，只关心业务；权限不通过直接抛 `AppException`，由 trigger 全局异常处理转 403。

### 5.5 完整代码示例

#### 5.5.1 登录用户值对象（domain 层，保持纯净）

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

#### 5.5.2 用户上下文（trigger 层）

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

#### 5.5.3 JWT 认证拦截器（trigger 层）

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

#### 5.5.4 鉴权注解 + AOP（trigger 层，声明式）

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

#### 5.5.5 权限领域服务 + 数据权限（domain 层）

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

#### 5.5.6 JWT 端口反转（domain 接口 + infrastructure 实现）

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

### 5.6 权限种子数据

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

## 6. 分层落地详解（逐模块怎么写）

### 6.1 types 层（已有，需补全）

**已有**：`Constants`、`ResponseCode`、`AppException`。

**补全**：

```java
// types/enums/ResponseCode.java —— 补充鉴权/业务码
SUCCESS("0000", "成功"),
UN_ERROR("0001", "未知失败"),
ILLEGAL_PARAMETER("0002", "非法参数"),
UNAUTHORIZED("0003", "未登录或token失效"),
FORBIDDEN("0004", "无权限"),
NOT_FOUND("0005", "资源不存在"),

// types/enums/OkrStatusEnum.java（新增）
DRAFT, ONGOING, DONE, CLOSED;
// 同理 KrStatusEnum / TaskStatusEnum / CycleStatusEnum / DataScopeEnum

// types/exception/AppException.java —— 修复 toString 残留旧包名
@Override
public String toString() {
    return "cn.bugstack.types.exception.AppException{code='" + code + "', info='" + info + "'}";
}
```

### 6.2 api 层（已有 Response，需补 DTO/VO + 接口契约）

```java
// api/IOkrObjectiveService.java —— 对外接口契约（Controller implements 它，抄 ai-mcp-gateway IMcpGatewayService 写法）
public interface IOkrObjectiveService {
    Response<Long> create(ObjectiveCreateDTO dto);
    Response<PageVO<ObjectiveVO>> page(ObjectiveQueryDTO query);
    Response<ObjectiveVO> detail(Long id);
    Response<Void> update(Long id, ObjectiveUpdateDTO dto);
    Response<Void> delete(Long id);
    Response<Void> publish(Long id);
    Response<Void> complete(Long id);
    Response<Void> close(Long id);
}

// api/dto/ObjectiveCreateDTO.java
@Data
public class ObjectiveCreateDTO {
    @NotBlank private String objectiveName;
    @NotNull private Long ownerUserId;
    private Long departmentId;
    @NotNull private Long cycleId;
    private String remark;
}

// api/vo/ObjectiveVO.java
@Data @Builder
public class ObjectiveVO {
    private Long id;
    private String objectiveName;
    private Long ownerUserId;
    private String ownerName;
    private Long departmentId;
    private Long cycleId;
    private BigDecimal progress;
    private String status;
    private String remark;
}
```

### 6.3 domain 层（核心，从空壳重建）

**清理**：删除脚手架 `xxx/`、`yyy/` 示例目录。

**聚合根示例** `domain/objective/model/aggregate/ObjectiveAggregate.java`（充血）：

```java
package cn.bugstack.domain.objective.model.aggregate;

import cn.bugstack.domain.keyresult.model.entity.KeyResultEntity;
import cn.bugstack.types.enums.OkrStatusEnum;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Objective 聚合根：聚合 KR 列表，内聚进度重算与状态机。
 * 行为内聚（充血），而非贫血的纯字段载体。
 */
@Getter
public class ObjectiveAggregate {
    private Long id;
    private String objectiveName;
    private Long ownerUserId;
    private Long departmentId;
    private Long cycleId;
    private BigDecimal progress;
    private OkrStatusEnum status;
    private List<KeyResultEntity> keyResults;  // 聚合内的 KR

    /** 重算 O 进度：Σ(权重/100 * KR完成率/100)*100 */
    public void recalculateProgress() {
        if (keyResults == null || keyResults.isEmpty()) {
            this.progress = BigDecimal.ZERO;
            return;
        }
        BigDecimal total = keyResults.stream()
                .map(kr -> kr.getWeight().multiply(kr.getCompletionRate())
                        .divide(BigDecimal.valueOf(10000)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(100));
        this.progress = total;
    }

    /** 状态机：草稿→进行中→完成→关闭 */
    public void publish() {
        if (status != OkrStatusEnum.DRAFT) throw new IllegalStateException("仅草稿可发布");
        this.status = OkrStatusEnum.ONGOING;
    }
    public void complete() {
        if (status != OkrStatusEnum.ONGOING) throw new IllegalStateException("仅进行中可完成");
        this.status = OkrStatusEnum.DONE;
    }
    public void close() {
        this.status = OkrStatusEnum.CLOSED;
    }
}
```

**仓储接口** `domain/objective/adapter/repository/IObjectiveRepository.java`（端口，抄 ai-mcp-gateway `IAuthRepository` 写法）：

```java
package cn.bugstack.domain.objective.adapter.repository;

import cn.bugstack.domain.objective.model.aggregate.ObjectiveAggregate;
import cn.bugstack.api.vo.ObjectiveVO;
import java.util.List;
import java.util.Set;

/**
 * Objective 仓储端口（领域接口）。
 * domain 只定义契约，实现在 infrastructure（端口反转）。
 */
public interface IObjectiveRepository {

    /** 新增 */
    Long insert(ObjectiveAggregate aggregate);

    /** 按ID加载聚合根（含 KR 列表） */
    ObjectiveAggregate findById(Long id);

    /** 更新 */
    int update(ObjectiveAggregate aggregate);

    /** 逻辑删除 */
    int delete(Long id);

    /** 分页查询（按部门范围过滤，data_scope 数据权限落地） */
    List<ObjectiveVO> queryPageByDeptIds(int page, int size, Set<Long> deptIds);

    List<ObjectiveVO> queryPage(int page, int size);
}
```

**推导领域服务** `domain/derivation/service/IOkrDerivationService.java`（OKR 原 derivation-worker 的领域化）：

```java
package cn.bugstack.domain.derivation.service;

/**
 * OKR 进度推导领域服务。
 * 对应 OKR 原 derivation-worker 的 OkrDerivationService，但移入 domain 层，
 * 由 trigger/listener 同步调用，不再依赖 RabbitMQ。
 */
public interface IOkrDerivationService {

    /** 重算 KR 完成率：已完成任务数/有效任务总数*100，并级联重算所属 O */
    void recalculateKrProgress(Long krId);

    /** 重算 O 进度：Σ(权重/100 * KR完成率/100)*100 */
    void recalculateObjectiveProgress(Long objectiveId);

    /** 应用 CheckIn 申报的 KR 完成率 */
    void applyCheckInResult(Long checkInId);
}
```

### 6.4 case 层（用例编排）

case 层应用服务是**事务边界 + 编排 + 事件发布**的承担者。除第 4.6.2 的 `TaskApplicationService` 外，其它子域同理。要点：

- 所有写方法加 `@Transactional(rollbackFor = Exception.class)`（补 ai-mcp-gateway 缺失的事务边界）。
- 编排多个领域服务/聚合根，**不写领域规则**（规则在聚合根/领域服务）。
- 发布领域事件（`ApplicationEventPublisher`）。
- 做 DTO ↔ 聚合根 的转换（转换逻辑可放聚合根静态工厂或独立 assembler）。
- 数据权限：查询前调 `IPermissionService.getCurrentUserManageableDepartmentIds()` 过滤。

```java
// case/objective/ObjectiveApplicationService.java（骨架）
@Slf4j @Service @RequiredArgsConstructor
public class ObjectiveApplicationService {

    private final IObjectiveRepository objectiveRepository;
    private final IPermissionService permissionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Long create(ObjectiveCreateDTO dto) {
        ObjectiveAggregate agg = ObjectiveAggregate.create(dto);  // 工厂
        Long id = objectiveRepository.insert(agg);
        // 发审计事件（AFTER_COMMIT 异步落库）
        eventPublisher.publishEvent(new OperationLogEvent(
            "okr-core", "OBJECTIVE", id, "CREATE",
            UserContext.getUserId(), null, JSON.toJSONString(dto),
            MDC.get("requestId"), null
        ));
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        ObjectiveAggregate agg = objectiveRepository.findById(id);
        agg.publish();                    // 聚合根状态机
        objectiveRepository.update(agg);  // 持久化
        eventPublisher.publishEvent(/* ObjectiveStatusChangedEvent + OperationLogEvent */);
    }
}
```

### 6.5 infrastructure 层（落地实现）

**已有**：17 个 DAO + 17 个 PO + 17 个 mapper XML（核心资产，直接用）。

**待补**：Repository 实现类（把 DAO/PO 适配成领域聚合根）、Redis、JwtGateway。

**Repository 实现示例** `infrastructure/adapter/repository/ObjectiveRepository.java`（抄 ai-mcp-gateway `AuthRepository` 写法：注入 DAO，PO ↔ 聚合根手工组装）：

```java
package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.objective.adapter.repository.IObjectiveRepository;
import cn.bugstack.domain.objective.model.aggregate.ObjectiveAggregate;
import cn.bugstack.domain.keyresult.model.entity.KeyResultEntity;
import cn.bugstack.infrastructure.dao.IOkrObjectiveDao;
import cn.bugstack.infrastructure.dao.IOkrKeyResultDao;
import cn.bugstack.infrastructure.dao.po.OkrObjectivePO;
import cn.bugstack.infrastructure.dao.po.OkrKeyResultPO;
import cn.bugstack.api.vo.ObjectiveVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Objective 仓储实现：实现 domain 的 IObjectiveRepository 端口。
 * 内部注入 MyBatis DAO，把 PO 手工组装成领域聚合根（端口反转）。
 */
@Repository
public class ObjectiveRepository implements IObjectiveRepository {

    @Resource
    private IOkrObjectiveDao okrObjectiveDao;        // ✅ 已有

    @Resource
    private IOkrKeyResultDao okrKeyResultDao;        // ✅ 已有，加载聚合内的 KR

    @Override
    public Long insert(ObjectiveAggregate agg) {
        OkrObjectivePO po = OkrObjectivePO.builder()
                .objectiveName(agg.getObjectiveName())
                .ownerUserId(agg.getOwnerUserId())
                .departmentId(agg.getDepartmentId())
                .cycleId(agg.getCycleId())
                .progress(agg.getProgress())
                .status(agg.getStatus().name().toLowerCase())
                .remark(agg.getRemark())
                .build();
        okrObjectiveDao.insert(po);   // useGeneratedKeys 回填 id
        return po.getId();
    }

    @Override
    public ObjectiveAggregate findById(Long id) {
        OkrObjectivePO po = okrObjectiveDao.queryById(id);  // ✅ 已有，含 is_deleted=0
        if (po == null) return null;
        // 组装聚合根：O + 其下 KR 列表
        List<KeyResultEntity> krs = okrKeyResultDao.queryByObjectiveId(id).stream()
                .map(this::toKrEntity).toList();
        return toAggregate(po, krs);
    }

    @Override
    public int update(ObjectiveAggregate agg) {
        OkrObjectivePO po = new OkrObjectivePO();
        po.setId(agg.getId());
        po.setProgress(agg.getProgress());
        po.setStatus(agg.getStatus().name().toLowerCase());
        return okrObjectiveDao.update(po);   // ✅ 已有动态 update
    }

    @Override
    public int delete(Long id) {
        return okrObjectiveDao.delete(id);   // ✅ 已有逻辑删除
    }

    // queryPage / queryPageByDeptIds 略，按 deptIds 拼 WHERE department_id IN (...)
    // toAggregate / toKrEntity：PO → 领域对象 的手工映射
}
```

> **关键**：infrastructure 的 Repository 实现完全复用已有的 `IOkrObjectiveDao`/`OkrObjectivePO`/`okr_objective_mapper.xml`，零改动。只需补 Repository 适配类 + `IOkrKeyResultDao.queryByObjectiveId` 等少量查询方法。

**Redis 补全** `infrastructure/redis/IRedisService.java` + `RedisService.java`（见第 9 章）。

### 6.6 trigger 层（入口适配）

- **http**：Controller，`implements` api 层接口契约，加 `@RequirePermission` 注解，调 case 层。
- **interceptor**：`JwtAuthInterceptor` + `UserContext`（见第 5 章）。
- **aspect**：`RequirePermission` + `RequirePermissionAspect`（见第 5 章）。
- **listener**：5 个领域事件监听器（见第 4 章）。
- **job**（可选）：`@Scheduled` 定时补偿任务，如重算 `apply_status=PENDING` 的 CheckIn、清理过期会话。

**Controller 示例** `trigger/http/OkrObjectiveController.java`：

```java
package cn.bugstack.trigger.http;

import cn.bugstack.api.IOkrObjectiveService;
import cn.bugstack.api.dto.*;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.vo.*;
import cn.bugstack.case.objective.ObjectiveApplicationService;
import cn.bugstack.trigger.aspect.RequirePermission;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/objectives")
@RequiredArgsConstructor
public class OkrObjectiveController implements IOkrObjectiveService {

    private final ObjectiveApplicationService appService;

    @PostMapping
    @RequirePermission("okr:objective:create")
    public Response<Long> create(@RequestBody @Valid ObjectiveCreateDTO dto) {
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(appService.create(dto))
                .build();
    }

    @GetMapping
    @RequirePermission("okr:objective:read")
    public Response<PageVO<ObjectiveVO>> page(ObjectiveQueryDTO query) {
        return Response.<PageVO<ObjectiveVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(appService.queryPage(query))
                .build();
    }

    @PostMapping("/{id}/publish")
    @RequirePermission("okr:objective:publish")
    public Response<Void> publish(@PathVariable Long id) {
        appService.publish(id);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).build();
    }
    // update/delete/complete/close 同理
}
```

### 6.7 app 层（启动 + 配置）

**已有**：`Application`、`GuavaConfig`、`ThreadPoolConfig`（`@EnableAsync`，复用给 `@Async` 审计监听器）、`ThreadPoolConfigProperties`。

**待补**：

- `WebMvcConfig`（注册 `JwtAuthInterceptor`，见第 5 章）。
- `RedisConfig`（见第 9 章）。
- `application-*.yml` 调整（库名、Redis，见第 9 章）。
- （可选）`GlobalExceptionHandler`：统一捕获 `AppException` 转 `Response`，401/403 分别处理。

```java
// app/config/GlobalExceptionHandler.java（可选，统一异常）
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public Response<Void> handle(AppException e) {
        return Response.<Void>builder().code(e.getCode()).info(e.getInfo()).build();
    }
}
```

## 7. 完整迁移路径（阶段化执行）

迁移按 9 个阶段推进，**每阶段独立可验收、可回滚**。建议严格按顺序，因为后阶段依赖前阶段的骨架与契约。

```
Phase 0  修复骨架问题        ──> 可编译可启动
Phase 1  types + api 补全     ──> 基础类型与接口契约就绪
Phase 2  domain 领域模型      ──> 聚合根/领域服务/仓储接口/事件定义
Phase 3  infrastructure 实现   ──> Repository 实现 + Redis
Phase 4  RBAC 鉴权骨架        ──> 认证 + 鉴权 + 数据权限
Phase 5  领域事件骨架         ──> 事件发布/监听打通
Phase 6  逐子域迁移业务       ──> IAM → Cycle → O → KR → Task → CheckIn → Progress → OperationLog
Phase 7  事件链路联调         ──> Task→KR→O→Progress 推导闭环
Phase 8  数据迁移 + 部署      ──> 上线
```

### Phase 0：修复骨架问题（阻断级，必先）

**目标**：让 OKR_REPLACE 能 `mvn clean install` 通过并可启动。

**任务**：

1. 根 `pom.xml` 删除第 18 行重复的 `<module>OKR_REPLACE-case</module>`（保留 17 行）。
2. `OKR_REPLACE-case/pom.xml` 删除 case 依赖自身的自循环 `<dependency>`。
3. `OKR_REPLACE-case` 包名 `cn.bugstack.ai.cases` → `cn.bugstack.case`（移动包 + 改 package-info）。
4. `application-dev.yml` 库名 `xfg_frame_archetype` → `okr_replace`。
5. `application-test.yml` / `application-prod.yml` 按环境补齐数据源 + mybatis 配置。
6. `AppException.toString()` 旧包名 `cn.bugstack.x.api.types.exception.XApiException` → `cn.bugstack.types.exception.AppException`。
7. 根 `pom.xml:205` prod profile `HeapDumpPath` 的 `fq-mall-activity-app` → `OKR_REPLACE`。
8. 删除 `OKR_REPLACE-domain` 下脚手架 `xxx/`、`yyy/` 示例目录。

**产出文件**：`pom.xml`、`OKR_REPLACE-case/pom.xml`、`AppException.java`、`application-*.yml`、case 包结构。

**验收**：`mvn clean install -DskipTests` 全模块 BUILD SUCCESS；`mvn -pl OKR_REPLACE-app spring-boot:run` 能连上 MySQL（需先 `okr_replace` 库 + 执行 schema.sql）。

**风险**：删 case 自循环依赖后若 case 真有代码引用自身需排查（当前为空壳，无影响）。

### Phase 1：types + api 基础补全

**目标**：通用类型与对外接口契约就绪。

**任务**：

1. `types/enums/ResponseCode` 补 `UNAUTHORIZED`/`FORBIDDEN`/`NOT_FOUND` 码。
2. `types/enums/` 新增 `OkrStatusEnum`/`KrStatusEnum`/`TaskStatusEnum`/`CycleStatusEnum`/`DataScopeEnum`/`AlignmentStatusEnum`。
3. `api/dto/` 新增各子域 DTO（`ObjectiveCreateDTO`/`ObjectiveQueryDTO`/`ObjectiveUpdateDTO` 等）。
4. `api/vo/` 新增各子域 VO + `PageVO<T>`。
5. `api/` 新增接口契约 `IOkrObjectiveService`/`IOkrKeyResultService`/`IOkrTaskService`/`IOkrCycleService`/`IUserService`/`IDepartmentService`/`ICheckInService`/`IProgressRecordService`/`IOperationLogService`/`IAuthService`（登录注册）。

**产出**：types/enums/*、api/dto/*、api/vo/*、api/I*Service.java。

**验收**：`mvn -pl OKR_REPLACE-types,OKR_REPLACE-api install` 通过；接口契约编译通过。

### Phase 2：domain 领域模型

**目标**：领域层充血化，定义聚合根、领域服务、仓储接口、领域事件。

**任务**（按第 3.4 子域划分）：

1. **聚合根/实体/值对象**：`ObjectiveAggregate`（含 KR 列表 + 进度重算 + 状态机）、`KeyResultEntity`、`TaskAggregate`、`CycleAggregate`、`CheckInAggregate`、`AlignmentAggregate`、`UserAggregate`、`DepartmentAggregate`、`ProgressRecordEntity`、`OperationLogEntity`。
2. **仓储端口**：`IObjectiveRepository`/`IKeyResultRepository`/`ITaskRepository`/`ICycleRepository`/`ICheckInRepository`/`IAlignmentRepository`/`IUserRepository`/`IDepartmentRepository`/`IProgressRecordRepository`/`IOperationLogRepository`。
3. **领域服务**：`IPermissionService`（第 5 章）、`IOkrDerivationService`（进度推导）、各子域 `IXxxService`。
4. **JWT 端口**：`IJwtGateway`（第 5 章）。
5. **领域事件**：`AbstractDomainEvent` + `TaskChangedEvent`/`KrChangedEvent`/`ProgressChangedEvent`/`CheckInSubmittedEvent`/`OperationLogEvent`/`ObjectiveStatusChangedEvent`（第 4 章）。
6. **值对象**：`LoginUserVO`、`DataScopeVO`、`PermissionSnapshotVO`。

**产出**：`domain/**` 全部子域代码。

**验收**：`mvn -pl OKR_REPLACE-domain install` 通过；domain 仅依赖 types（无 infrastructure 依赖，端口反转成立）。

**风险**：聚合根设计易过度建模——只为核心域（Objective/KR/Task）做充血，简单 CRUD 子域（Department/Permission）可保持轻量。

### Phase 3：infrastructure 实现

**目标**：实现 domain 端口，接通 Redis。

**任务**：

1. **Repository 实现**：每个 `IXxxRepository` 写一个 `XxxRepository implements IXxxRepository`，注入已有 DAO，PO ↔ 聚合根手工组装（第 6.5 示例）。
2. **DAO 补查询方法**：已有 17 个 DAO 多为 insert/queryById/update/delete，需补业务查询（如 `IOkrKeyResultDao.queryByObjectiveId`、`IOkrTaskDao.queryByKrId`、`ISysUserRoleDao.queryRoleCodesByUserId`、`ISysRolePermissionDao.queryPermCodesByRoleIds`），同步补 mapper XML。
3. **JwtGateway 实现**：`infrastructure/adapter/gateway/JwtGateway implements IJwtGateway`，jjwt 签发/解析 + 查五表组装 `LoginUserVO`。
4. **Redis**：`IRedisService`/`RedisService`（第 9 章），用于权限快照缓存、登录 token 黑名单等。
5. **PO 补字段**：若 domain 值对象需要 PO 没有的字段（如 `ownerName`），用 join 查询或应用层组装，不擅自改表。

**产出**：`infrastructure/adapter/repository/*`、`infrastructure/adapter/gateway/JwtGateway`、`infrastructure/redis/*`、补充的 DAO 方法 + mapper XML。

**验收**：`mvn -pl OKR_REPLACE-infrastructure install` 通过；单元测试 Repository 能查到数据（连 dev 库）。

**风险**：权限查询频繁，必须缓存（Redis 5min）`LoginUserVO`，否则每个请求都查五表。

### Phase 4：RBAC 鉴权骨架

**目标**：认证 + 鉴权 + 数据权限三层打通。

**任务**：

1. `trigger/interceptor/UserContext` + `JwtAuthInterceptor`（第 5.5.2-5.5.3）。
2. `trigger/aspect/RequirePermission` + `RequirePermissionAspect`（第 5.5.4）。
3. `app/config/WebMvcConfig` 注册拦截器 + `GlobalExceptionHandler`。
4. `infrastructure` 实现 `IPermissionService`（查 `data_scope` + 部门树递归取 `manageableDeptIds`）。
5. `case/iam/AuthApplicationService` 实现登录（`IJwtGateway.sign`）/注册。
6. `data.sql` 初始化 RBAC 种子数据（第 5.6）。

**产出**：`trigger/interceptor/*`、`trigger/aspect/*`、`app/config/WebMvcConfig`、`infrastructure/.../PermissionService`、`data.sql`。

**验收**：`POST /api/auth/login` 返回 token；带 token 访问 `/api/objectives` 通过认证；无 `okr:objective:create` 权限的用户调创建接口返回 403。

**风险**：JWT secret 必须从配置读取（修复 OKR 原 secret 占位坑），过期时间设为合理值（如 7200s，非 100 年）。

### Phase 5：领域事件骨架

**目标**：事件发布/监听机制打通（先跑通框架，业务逻辑 Phase 6 填）。

**任务**：

1. 确认 `app/ThreadPoolConfig` 的 `@EnableAsync` 生效（`@Async` 监听器依赖）。
2. `trigger/listener/` 写 5 个监听器骨架（先 `log.info` 打印事件，不接业务）。
3. 在一个 case 方法里 `publishEvent` 测试事件能被监听器收到。
4. 验证 `@TransactionalEventListener(AFTER_COMMIT)` 在事务回滚时不触发（写个失败用例）。

**产出**：`trigger/listener/*` 5 个骨架类。

**验收**：发布事件后监听器日志打印；模拟事务回滚后 `AFTER_COMMIT` 监听器不执行。

### Phase 6：逐子域迁移业务

**目标**：把 OKR 各微服务的业务逻辑搬到对应子域。**按依赖顺序**迁移（被依赖的先做）。

**迁移顺序与映射**（OKR 微服务 → OKR_REPLACE 子域）：

| 顺序 | OKR 源服务 | OKR_REPLACE 子域 | 关键迁移点 |
| --- | --- | --- | --- |
| 1 | `iam-service`（用户/部门） | `domain/iam` + `case/iam` | User/Department CRUD；登录注册；PermissionService |
| 2 | `okr-core-service`（Cycle） | `domain/cycle` + `case/cycle` | Cycle 状态机（draft/enabled/disabled/closed/archived） |
| 3 | `okr-core-service`（Objective） | `domain/objective` + `case/objective` | O 聚合根；创建/发布/完成/关闭；数据权限过滤 |
| 4 | `okr-core-service`（KR） | `domain/keyresult` + `case/keyresult` | KR 权重校验（同 O 下权重和=100）；完成率 |
| 5 | `task-service` | `domain/task` + `case/task` | Task CRUD；状态变更触发 `TaskChangedEvent`；**接口 RESTful 化**（`/api/task/addTask` → `POST /api/tasks`） |
| 6 | `okr-core-service`（Alignment） | `domain/alignment` + `case/alignment` | **重新实现**（OKR 原为占位）；创建/取消对齐；上下级查询 |
| 7 | `checkin-service` | `domain/checkin` + `case/checkin` | CheckIn 提交触发 `CheckInSubmittedEvent`；修复事务后发送 |
| 8 | `progress-service` | `domain/progress` + `case/progress` | `ProgressChangedEvent` 监听落 `okr_progress_record`（只追加） |
| 9 | `operation-log-service` | `domain/operationlog` + `case/operationlog` | `OperationLogEvent` 监听落 `okr_operation_log`（`@Async AFTER_COMMIT`） |
| 10 | `derivation-worker` | `domain/derivation` + `trigger/listener` | 进度推导领域服务；由监听器同步调用，**不再用 RabbitMQ** |

**任务（每个子域统一）**：

1. 搬 OKR 原 Service 业务逻辑到 case 层应用服务（去掉跨服务 Feign 调用，改进程内调 Repository/领域服务）。
2. 领域规则下沉到聚合根/领域服务（如 KR 权重和校验、O 进度重算公式）。
3. Controller 按附录 D RESTful 化接口路径。
4. 写操作发对应领域事件 + `OperationLogEvent`。
5. 接 `@RequirePermission` 权限注解。

**产出**：`domain/<子域>/**`、`case/<子域>/**`、`trigger/http/<子域>Controller`、补充的 Repository 实现。

**验收**：每个子域迁移后，用 Postman 验证 CRUD 接口；接口路径符合附录 D；权限注解生效。

**风险**：OKR 原 KR/Task 接口命名不规范（`/api/KR/create/{ObjectiveId}`），迁移时**必须** RESTful 化，前端需同步改（见附录 D 映射表）。

### Phase 7：事件链路联调

**目标**：`Task → KR → O → Progress` 推导闭环 + 审计闭环。

**任务**：

1. 监听器接真实业务（替换 Phase 5 的 `log` 骨架）。
2. 端到端验证：更新 Task 状态为 done → KR 完成率重算 → O 进度重算 → `okr_progress_record` 追加 → `okr_operation_log` 追加。
3. 验证事务回滚场景：Task 更新失败 → 推导不触发（`AFTER_COMMIT` 生效）。
4. 验证审计异步：主接口返回不被审计落库阻塞。
5. 幂等验证：同 `eventId` 重复事件不重复落库。

**产出**：联调报告 + 修复的监听器/推导服务。

**验收**：一次 Task 变更触发完整链路，5 张表（task/key_result/objective/progress_record/operation_log）状态正确。

**风险**：同步级联事件若链路过深（Task→KR→O→Progress 4 层），事务变大；若性能不达标，可将 Progress 落流水改为 `AFTER_COMMIT` 异步。

### Phase 8：数据迁移 + 部署

**目标**：OKR 生产数据迁入 OKR_REPLACE，部署上线。

**任务**：

1. 按第 8 章做数据迁移脚本（OKR 13 表 → OKR_REPLACE 16 表）。
2. 按第 9 章调整配置 + docker-compose（`replace` 目录）。
3. 灰度验证：双写或影子流量对比。
4. 切流：前端指向 OKR_REPLACE 网关地址。

**产出**：数据迁移脚本、`docs/dev-ops/replace/docker-compose-*.yml`、上线方案。

**验收**：数据校验通过（行数/关键记录抽检）；线上接口可用；监控告警接入。

**风险**：OKR 共享库 `okr_backend` 迁移到 `okr_replace` 期间需停写或双写；RBAC 五表数据从零初始化（原 OKR 没启用），需手工配置角色-权限映射。

---

## 8. 数据迁移

### 8.1 表结构对齐（OKR 13 表 vs OKR_REPLACE 16 表）

| OKR 源表 | OKR_REPLACE 目标表 | 差异 | 迁移动作 |
| --- | --- | --- | --- |
| `sys_user` | `sys_user` | OKR_REPLACE 多了标准索引；`role` 字段保留 | 直迁，密码 BCrypt 不变 |
| `sys_department` | `sys_department` | 一致 | 直迁 |
| `sys_role` | `sys_role` | 一致（OKR 已建未用，需补种子数据） | 补 admin/dept_admin/user 三角色 |
| `sys_permission` | `sys_permission` | OKR 无数据，需新建权限点 | 按附录 C 初始化权限点 |
| `sys_user_role` | `sys_user_role` | OKR 无数据 | 根据原 `sys_user.role` 字段反推写入（admin→admin 角色） |
| `sys_role_permission` | `sys_role_permission` | OKR 无数据 | admin 角色赋全部权限 |
| `okr_objective` | `okr_objective` | 一致（OKR_REPLACE 默认 status='ongoing'） | 直迁 |
| `okr_key_result` | `okr_key_result` | 一致 | 直迁 |
| `okr_task` | `okr_task` | 一致 | 直迁 |
| `okr_objective_user` | `okr_objective_user` | 一致 | 直迁 |
| `okr_task_user` | `okr_task_user` | 一致 | 直迁 |
| `okr_cycle` | `okr_cycle` | OKR_REPLACE 唯一键含 `department_id` | 直迁，注意唯一约束 |
| `okr_objective_alignment` | `okr_objective_alignment` | OKR 原为占位，可能无数据 | 直迁或新建 |
| `okr_progress_record` | `okr_progress_record` | OKR_REPLACE 字段一致 | 直迁 |
| `okr_check_in` | `okr_check_in` | OKR_REPLACE 多 `apply_message`/`applied_at` | 直迁，旧记录填默认值 |
| `okr_check_in_item` | `okr_check_in_item` | OKR_REPLACE 多 `apply_message` | 直迁 |
| — | `okr_operation_log` | OKR_REPLACE 新增 | 直迁 OKR 原审计数据（如有） |

### 8.2 数据迁移脚本思路

OKR 原库 `okr_backend` → 目标库 `okr_replace`，分三步：

```sql
-- Step 1: 结构初始化（在 okr_replace 库执行 OKR_REPLACE 的 schema.sql）
-- 已有：docs/dev-ops/mysql/sql/schema.sql

-- Step 2: 直迁业务数据（跨库 INSERT INTO ... SELECT）
-- 需在目标库执行，源库用 库名.表名 跨库引用（同 MySQL 实例）
INSERT INTO okr_replace.sys_user(username, account, password, role, department_id, status)
SELECT username, account, password, role, department_id, status FROM okr_backend.sys_user WHERE is_deleted=0;

INSERT INTO okr_replace.sys_department(id, org_id, parent_id, dept_name, dept_code, leader_user_id, sort_order, status)
SELECT id, org_id, parent_id, dept_name, dept_code, leader_user_id, sort_order, status
FROM okr_backend.sys_department WHERE is_deleted=0;

-- okr_objective / okr_key_result / okr_task / okr_cycle / okr_progress_record / okr_check_in 等同理
-- 注意：id 自增列需保留原值，迁移时 SET @@session.sql_mode 去掉 NO_AUTO_VALUE_ON_ZERO 或显式带 id

-- Step 3: RBAC 数据初始化（OKR 原未启用，从零按第 5.6 节建）
-- 角色、权限点、角色-权限、根据 sys_user.role 反推 sys_user_role
INSERT INTO okr_replace.sys_user_role(user_id, role_id)
SELECT u.id, r.id FROM okr_replace.sys_user u, okr_replace.sys_role r
WHERE u.role = r.role_code;  -- 把 role 字符串映射到 sys_role
```

**迁移注意事项**：

- **停写迁移**：迁移期间 OKR 停写或只读，避免增量丢失。
- **id 保留**：业务表 id 必须保留原值（外键依赖），迁移时显式带 id 列。
- **逻辑删除**：只迁 `is_deleted=0` 的数据，或全量迁保留 `is_deleted` 标记。
- **密码**：`sys_user.password` 是 BCrypt 哈希，直迁无需重新加密。
- **校验**：迁移后 `SELECT COUNT(*)` 对比源/目标行数，抽检关键记录。

### 8.3 RBAC 种子数据

见第 5.6 节。重点是：OKR 原 RBAC 五表是空的（代码没启用），迁移时从零初始化：

- 3 个角色：admin(all) / dept_admin(dept_and_below) / user(self)。
- 权限点按附录 C 全量初始化。
- admin 角色赋全部权限。
- 根据 `sys_user.role` 字段反推 `sys_user_role`（admin 用户 → admin 角色）。

## 9. 配置与部署

### 9.1 application.yml 调整

`OKR_REPLACE-app/src/main/resources/application-dev.yml` 修改（基于现有文件）：

```yaml
server:
  port: 8091

thread:
  pool:
    executor:
      config:
        core-pool-size: 20
        max-pool-size: 50
        keep-alive-time: 5000
        block-queue-size: 5000
        policy: CallerRunsPolicy   # @Async 审计监听器复用此线程池

spring:
  datasource:
    username: root
    password: 123456
    # ⚠️ 库名从 xfg_frame_archetype 改为 okr_replace
    url: jdbc:mysql://127.0.0.1:3306/okr_replace?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=true
    driver-class-name: com.mysql.cj.jdbc.Driver
  hikari:
    pool-name: OKR_REPLACE_HikariCP
    minimum-idle: 15
    idle-timeout: 180000
    maximum-pool-size: 25
    auto-commit: true
    max-lifetime: 1800000
    connection-timeout: 30000
    connection-test-query: SELECT 1
  # ⚠️ 新增 Redis 配置（OKR_REPLACE 当前完全缺失）
  data:
    redis:
      host: 127.0.0.1
      port: 16379            # 对齐 docker-compose-environment.yml 的映射端口
      password:
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2

mybatis:
  mapper-locations: classpath:/mybatis/mapper/*.xml
  config-location: classpath:/mybatis/config/mybatis-config.xml

# ⚠️ 新增 JWT 配置（修复 OKR 原 secret 占位 + 过期 100 年的坑）
jwt:
  secret: ${JWT_SECRET:please-change-me-in-production-at-least-32-chars}
  issuer: okr-replace
  expiration: 7200           # 秒，2 小时（OKR 原为 3153600000≈100年，必改）

logging:
  level:
    root: info
    cn.bugstack: debug       # 开发期看事件链路日志
  config: classpath:logback-spring.xml
```

`application-test.yml` / `application-prod.yml`：取消数据源与 mybatis 的注释，按环境填入；prod 的 JWT secret 必须用环境变量 `${JWT_SECRET}` 注入。

### 9.2 Redis 接通

**1. pom 依赖**（根 pom `dependencyManagement` + `OKR_REPLACE-infrastructure/pom.xml` 引入）：

```xml
<!-- 根 pom dependencyManagement 新增 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>3.4.3</version>   <!-- 与父 POM 对齐 -->
</dependency>

<!-- OKR_REPLACE-infrastructure/pom.xml 新增 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**2. RedisService** `infrastructure/redis/IRedisService.java` + `RedisService.java`：

```java
package cn.bugstack.infrastructure.redis;

public interface IRedisService {
    void set(String key, Object value, long timeoutSeconds);
    <T> T get(String key, Class<T> clazz);
    void del(String key);
    boolean exists(String key);
}

// RedisService 用 RedisTemplate 实现，略
```

**3. RedisConfig** `app/config/RedisConfig.java`：配置 `RedisTemplate` 序列化（Jackson2JsonRedisSerializer）。

**4. 用途**：

- `LoginUserVO` 权限快照缓存（key=`user:perm:{userId}`，TTL 5min），避免每个请求查五表。
- JWT 黑名单（登出/改密时 token 提前失效，key=`jwt:blacklist:{token}`，TTL=剩余过期时间）。
- 部门树缓存（key=`dept:tree:{orgId}`，TTL 10min）。

> OKR_REPLACE 的 `docker-compose-environment.yml` 已起 Redis（端口 16379），代码侧补依赖+配置+RedisService 即可接通。

### 9.3 docker-compose（replace 目录）

`docs/dev-ops/replace/` 目录用于迁移专属部署。建议放置：

```
docs/dev-ops/replace/
├── OKR_REPLACE-迁移与DDD领域事件架构设计文档.md   # ⬅ 本文档
├── docker-compose-app.yml          # 应用容器（复制并改写自 ../docker-compose-app.yml）
├── docker-compose-environment.yml  # 基础环境（复用 ../，MySQL+Redis）
├── mysql/
│   └── schema.sql -> ../../mysql/sql/schema.sql   # 软链或复制
├── app/
│   ├── start.sh
│   └── stop.sh
└── data-migration/                 # 数据迁移脚本
    ├── 01-init-schema.sql
    ├── 02-migrate-business-data.sql
    └── 03-init-rbac-seed.sql
```

`replace/docker-compose-app.yml`（基于现有 `../docker-compose-app.yml` 改写）：

```yaml
version: '3.8'
services:
  okr-replace:
    image: system/okr_replace:1.0-SNAPSHOT
    container_name: okr-replace
    ports:
      - "8091:8091"
    environment:
      - TZ=Asia/Shanghai
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=${JWT_SECRET}              # 从 .env 注入，勿硬编码
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/okr_replace?...
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PORT=6379
    depends_on:
      - mysql
      - redis
    networks:
      - okr-net
networks:
  okr-net:
    external: true
```

### 9.4 启动验证清单

1. `docker compose -f docker-compose-environment.yml up -d`（起 MySQL 13306 + Redis 16379）。
2. 在 MySQL 建 `okr_replace` 库并执行 `schema.sql` + `data.sql`（RBAC 种子）。
3. `mvn clean install -DskipTests` 全模块构建。
4. `mvn -pl OKR_REPLACE-app spring-boot:run`（dev profile）。
5. 验证：
   - `GET /api/health` 返回 200（白名单免鉴权）。
   - `POST /api/auth/login`（admin/admin123）返回 JWT。
   - 带 JWT `GET /api/objectives` 返回 200 + 数据。
   - 无 JWT 访问 `/api/objectives` 返回 401。
   - 用 `user` 角色账号调 `POST /api/objectives`（需 `okr:objective:create`，user 无此权限）返回 403。
   - 更新一个 Task 状态，查 `okr_progress_record` 与 `okr_operation_log` 有新增（事件链路通）。

---

## 10. 风险清单与遗留技术债

### 10.1 迁移风险清单

| 风险 | 等级 | 影响 | 缓解 |
| --- | --- | --- | --- |
| 同步事件级联事务过大（Task→KR→O→Progress 4 层） | 🟡 中 | 长事务锁竞争 | Progress 落流水改 `AFTER_COMMIT` 异步；推导用只读查询 |
| `@Async` 审计失败丢日志 | 🟡 中 | 审计不完整 | 落库失败写本地文件兜底；定时任务补偿 |
| 权限查询无缓存导致 DB 压力 | 🟡 中 | 每请求查五表 | Redis 缓存 `LoginUserVO`（5min），登出/改密清缓存 |
| JWT secret 泄漏 | 🔴 高 | token 可伪造 | prod 从环境变量注入，不进 git |
| 数据迁移期间增量丢失 | 🔴 高 | 数据不一致 | 停写迁移或双写校验 |
| OKR 原 KR/Task 接口路径变更 | 🟡 中 | 前端需同步改 | 附录 D 提供完整映射表，前端一次性切换 |
| 聚合根过度建模 | 🟢 低 | 开发成本上升 | 仅核心域充血，简单 CRUD 子域保持轻量 |
| `xfg-wrench-starter-design-framework` 未使用 | 🟢 低 | 无 | 可选用于 case 层策略树编排，不强制 |

### 10.2 遗留技术债（迁移后处理）

| 技术债 | 来源 | 建议处理 |
| --- | --- | --- |
| 无数据库迁移工具 | OKR 用 schema.sql 幂等模式 | 迁移完成后引入 Flyway，版本化管理 schema |
| Alignment 功能 | OKR 原为占位 | Phase 6 重新实现（上下级对齐树查询） |
| CheckIn 与 Task 推导优先级 | OKR 未明确 | 文档约定：CheckIn 申报值优先于 Task 推导值，冲突时以 CheckIn 为准 |
| Knife4j/OpenAPI 未引入 | OKR_REPLACE pom 无 | 后续引入 springdoc-openapi，替代手写接口文档 |
| 前端联调 | OKR 前端按旧接口 | 按附录 D 接口映射改造前端 |
| 测试覆盖 | OKR_REPLACE 仅 `ApiTest` | 补领域服务单测 + 事件链路集成测试 |

### 10.3 未来演进（保留出口）

- **拆微服务**：若未来单体内某些子域（如 derivation 计算密集）需独立扩容，把 `@EventListener` 换 `@RabbitListener`、`eventPublisher.publishEvent` 换 `rabbitTemplate.convertAndSend`，事件 record 不变（这正是第 4.4 节保留 MQ 可选出口的意义）。
- **CQRS**：查询走读库/Redis 缓存，写走领域模型，提升读性能。
- **多租户**：表已有 `org_id` 字段，未来按 `org_id` 隔离即可支持 SaaS。

---

## 附录 A：完整目录树

```
OKR_REPLACE/
├── pom.xml
├── OKR_REPLACE-types/src/main/java/cn/bugstack/types/
│   ├── common/Constants.java
│   ├── enums/{ResponseCode, OkrStatusEnum, KrStatusEnum, TaskStatusEnum, CycleStatusEnum, DataScopeEnum, AlignmentStatusEnum}.java
│   └── exception/AppException.java
│
├── OKR_REPLACE-api/src/main/java/cn/bugstack/api/
│   ├── {IOkrObjectiveService, IOkrKeyResultService, IOkrTaskService, IOkrCycleService,
│   │    IUserService, IDepartmentService, ICheckInService, IProgressRecordService,
│   │    IOperationLogService, IAuthService}.java
│   ├── dto/{ObjectiveCreateDTO, ObjectiveQueryDTO, ...}.java
│   ├── vo/{ObjectiveVO, PageVO, ...}.java
│   └── response/Response.java
│
├── OKR_REPLACE-domain/src/main/java/cn/bugstack/domain/
│   ├── event/AbstractDomainEvent.java
│   ├── iam/
│   │   ├── model/{aggregate/UserAggregate, aggregate/DepartmentAggregate, valobj/LoginUserVO, valobj/DataScopeVO}
│   │   ├── service/{IIamService, IPermissionService}
│   │   └── adapter/{repository/IIamRepository, gateway/IJwtGateway}
│   ├── cycle/{model, service, adapter/repository/ICycleRepository}
│   ├── objective/
│   │   ├── model/{aggregate/ObjectiveAggregate, entity, valobj, event/ObjectiveProgressChangedEvent}
│   │   ├── service/IObjectiveService
│   │   └── adapter/repository/IObjectiveRepository
│   ├── keyresult/{model/event/KrChangedEvent, service, adapter/repository/IKeyResultRepository}
│   ├── task/{model/{aggregate/TaskAggregate, event/TaskChangedEvent}, service, adapter/repository/ITaskRepository}
│   ├── alignment/{model, service, adapter/repository/IAlignmentRepository}
│   ├── checkin/{model/{aggregate/CheckInAggregate, event/CheckInSubmittedEvent}, service, adapter/repository/ICheckInRepository}
│   ├── progress/{model/event/ProgressChangedEvent, service, adapter/repository/IProgressRecordRepository}
│   ├── operationlog/{model/event/OperationLogEvent, adapter/repository/IOperationLogRepository}
│   └── derivation/service/{IOkrDerivationService, OkrDerivationService}
│
├── OKR_REPLACE-case/src/main/java/cn/bugstack/case/
│   ├── iam/{IamApplicationService, AuthApplicationService}
│   ├── cycle/CycleApplicationService
│   ├── objective/ObjectiveApplicationService
│   ├── keyresult/KeyResultApplicationService
│   ├── task/TaskApplicationService
│   ├── alignment/AlignmentApplicationService
│   ├── checkin/CheckInApplicationService
│   └── {progress, operationlog}/...
│
├── OKR_REPLACE-infrastructure/src/main/java/cn/bugstack/infrastructure/
│   ├── adapter/repository/{ObjectiveRepository, KeyResultRepository, TaskRepository, CycleRepository,
│   │                       CheckInRepository, AlignmentRepository, UserRepository, DepartmentRepository,
│   │                       ProgressRecordRepository, OperationLogRepository, PermissionService}.java
│   ├── adapter/gateway/JwtGateway.java
│   ├── dao/                          # ✅ 已有 17 个 @Mapper 接口
│   ├── dao/po/                       # ✅ 已有 17 个 PO
│   └── redis/{IRedisService, RedisService}.java
│
├── OKR_REPLACE-trigger/src/main/java/cn/bugstack/trigger/
│   ├── http/{OkrObjectiveController, OkrKeyResultController, OkrTaskController, OkrCycleController,
│   │         UserController, DepartmentController, CheckInController, AuthController, ...}.java
│   ├── interceptor/{JwtAuthInterceptor, UserContext}.java
│   ├── aspect/{RequirePermission, RequirePermissionAspect}.java
│   ├── job/ProgressRecalculateJob.java   # 可选
│   └── listener/                          # ✅ 本次补实现
│       ├── TaskChangedEventListener.java
│       ├── KrChangedEventListener.java
│       ├── ProgressChangedEventListener.java
│       ├── CheckInSubmittedEventListener.java
│       └── OperationLogEventListener.java
│
└── OKR_REPLACE-app/src/main/java/cn/bugstack/
    ├── Application.java
    └── config/{WebMvcConfig, RedisConfig, ThreadPoolConfig, GuavaConfig, GlobalExceptionHandler}.java
    # resources: application*.yml, mybatis/mapper/*.xml, logback-spring.xml
```

## 附录 B：领域事件清单

| 事件 | 所属子域 | 字段 | 发布点 | 监听器 | 一致性 |
| --- | --- | --- | --- | --- | --- |
| `TaskChangedEvent` | task | eventId, occurredOn, krId | `TaskApplicationService` 写操作 | `TaskChangedEventListener` → `IOkrDerivationService.recalculateKrProgress` | 同步强一致 |
| `KrChangedEvent` | keyresult | eventId, occurredOn, objectiveId | 推导服务重算 KR 后 | `KrChangedEventListener` → `IOkrDerivationService.recalculateObjectiveProgress` | 同步强一致 |
| `ProgressChangedEvent` | progress | target_type, target_id, old, new, source_type, operator_id | 推导服务重算 O/KR 后 | `ProgressChangedEventListener` → 落 `okr_progress_record` | AFTER_COMMIT 最终一致 |
| `CheckInSubmittedEvent` | checkin | eventId, checkInId | `CheckInApplicationService` 提交 | `CheckInSubmittedEventListener` → `IOkrDerivationService.applyCheckInResult` | AFTER_COMMIT 最终一致 |
| `OperationLogEvent` | operationlog | serviceName, resourceType, resourceId, action, operatorId, beforeJson, afterJson, requestId, ip | 所有写操作 | `OperationLogEventListener` → 落 `okr_operation_log` | @Async AFTER_COMMIT |
| `ObjectiveStatusChangedEvent` | objective | objectiveId, oldStatus, newStatus | O 状态机变更 | 可选：通知前端/统计 | AFTER_COMMIT |

## 附录 C：权限码清单

权限码格式：`<域>:<资源>:<动作>`，对应 `sys_permission.perm_code`。

| 域 | 权限码 | 说明 |
| --- | --- | --- |
| 认证 | `auth:login`、`auth:register` | 登录注册（白名单，无需鉴权） |
| 用户 | `iam:user:read`、`iam:user:create`、`iam:user:update`、`iam:user:delete`、`iam:user:manage`(含改角色/状态/部门/重置密码) | 用户管理 |
| 部门 | `iam:dept:read`、`iam:dept:create`、`iam:dept:update`、`iam:dept:delete` | 部门管理 |
| 周期 | `okr:cycle:read`、`okr:cycle:create`、`okr:cycle:update`、`okr:cycle:delete`、`okr:cycle:publish`、`okr:cycle:close`、`okr:cycle:archive` | 周期管理 |
| 目标 | `okr:objective:read`、`okr:objective:create`、`okr:objective:update`、`okr:objective:delete`、`okr:objective:publish`、`okr:objective:complete`、`okr:objective:close` | O 管理 |
| KR | `okr:keyresult:read`、`okr:keyresult:create`、`okr:keyresult:update`、`okr:keyresult:delete` | KR 管理 |
| 任务 | `okr:task:read`、`okr:task:create`、`okr:task:update`、`okr:task:delete`、`okr:task:cancel` | Task 管理 |
| 对齐 | `okr:alignment:read`、`okr:alignment:create`、`okr:alignment:cancel` | 对齐管理 |
| Check-in | `okr:checkin:read`、`okr:checkin:create` | Check-in |
| 审计 | `okr:operationlog:read` | 操作日志查看（仅 admin） |
| 权限管理 | `iam:role:manage`、`iam:permission:manage` | 角色/权限点管理（仅 admin） |

**角色-权限分配建议**：

| 角色 | data_scope | 权限范围 |
| --- | --- | --- |
| admin | all | 全部权限码 |
| dept_admin | dept_and_below | 本部门 OKR/Task/CheckIn 读写 + 用户查看；无 iam:user:manage / iam:role:manage |
| user | self | 自身相关 OKR/Task/CheckIn 读写 |

## 附录 D：API 迁移映射表

OKR 原接口（命名不规范）→ OKR_REPLACE 接口（RESTful）。前端按此表改造。

| OKR 原接口 | 方法 | OKR_REPLACE 接口 | 方法 | 说明 |
| --- | --- | --- | --- | --- |
| `/api/auth/login` | POST | `/api/auth/login` | POST | 不变 |
| `/api/auth/register` | POST | `/api/auth/register` | POST | 不变 |
| `/api/users`、`/api/users/{id}` | GET/PUT | `/api/users`、`/api/users/{id}` | GET/PUT/PATCH | 规范化 |
| `/api/departments` | - | `/api/departments` | GET/POST/PUT/DELETE | 树接口 `/api/departments/tree` |
| `/api/cycles` | - | `/api/cycles` | GET/POST/PUT/DELETE | 状态动作 `/{id}/publish`/`/disable`/`/close`/`/archive` |
| `/api/objectives` | - | `/api/objectives` | GET/POST/PUT/DELETE | `/selectable-for-kr`→`/api/objectives/selectable`；`/{id}/publish`/`/complete`/`/close` |
| `/api/KR/create/{ObjectiveId}` | POST(未限定) | `/api/objectives/{objectiveId}/key-results` | POST | **改为嵌套 RESTful** |
| `/api/KR/delete` | POST | `/api/key-results/{id}` | DELETE | **改为 DELETE** |
| `/api/KR/update` | POST | `/api/key-results/{id}` | PUT | **改为 PUT** |
| `/api/KR/select/{ObjectiveId}` | GET(未限定) | `/api/objectives/{objectiveId}/key-results` | GET | 嵌套查询 |
| `/api/task/addTask` | POST | `/api/tasks` | POST | **RESTful 化** |
| `/api/task/updateTask` | POST | `/api/tasks/{id}` | PUT | |
| `/api/task/cancelTask` | POST | `/api/tasks/{id}/cancel` | POST | 动作型 |
| `/api/task/deleteTask` | POST | `/api/tasks/{id}` | DELETE | |
| `/api/task/getTaskById?id=` | GET | `/api/tasks/{id}` | GET | 路径参数 |
| `/api/task/getkr?krId=` | GET | `/api/key-results/{id}/tasks` 或 `/api/tasks?krId=` | GET | 嵌套或查询参数 |
| `/api/task/getuser?userId=` | GET | `/api/users/{id}/tasks` | GET | 嵌套 |
| `/api/task/getDepartmentTask?deptId=` | GET | `/api/departments/{id}/tasks` | GET | 嵌套 |
| `/api/task-user/getTaskByUserId` | GET | `/api/users/{id}/tasks` | GET | 合并 |
| `/api/count/getCount` | GET | `/api/tasks/count` 或聚合到 `/api/stats/tasks` | GET | |
| `/api/check-ins` | - | `/api/check-ins` | GET/POST | `/{id}` |
| `/api/progress-records` | GET | `/api/progress-records` | GET | `/{id}` |
| `/api/operation-logs` | GET | `/api/operation-logs` | GET | `/{id}` |
| `/api/okr-alignment`（占位） | GET | `/api/alignments` | GET/POST | **重新实现** |
| `/internal/**` | - | 删除 | - | 单体内无跨服务调用，内部接口全部删除，改进程内方法调用 |

---

> **文档结束**。本文档为 OKR → OKR_REPLACE 迁移的完整设计基线，覆盖架构对齐、领域事件驱动、RBAC、分层落地、阶段化迁移路径、数据迁移与部署。落地时按 Phase 0 → Phase 8 顺序执行，每阶段验收通过再进入下一阶段。如对架构选型（如事件机制、鉴权位置）有调整，建议以本文档为基线评审后修订版本号。
