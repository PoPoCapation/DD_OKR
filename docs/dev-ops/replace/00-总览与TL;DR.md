# 第 0 章 · 总览与 TL;DR

> OKR_REPLACE 迁移与 DDD 领域事件架构设计文档 · 第 0 章
> ⬅ 上一章：[README 索引](README.md) ｜ ➡ 下一章：[第 1 章 迁移背景与目标](01-迁移背景与目标.md)

| 项 | 值 |
| --- | --- |
| 文档版本 | v1.0 |
| 编写日期 | 2026-07-05 |
| 源项目 | `D:\project\OKR`（9 个微服务，Spring Boot 3.4.1 + Spring Cloud + RabbitMQ） |
| 参考项目 | `D:\project\MCP\mcp\ai-mcp-gateway`（七模块 DDD，端口反转写法） |
| 目标项目 | `D:\project\OKR_REPLACE`（单体 DDD，Spring Boot 3.4.3 + Java 17） |
| 状态 | 待评审 / 落地实施中 |

## 文档目录

- [00-总览与TL;DR](00-总览与TL;DR.md)
- [01-迁移背景与目标](01-迁移背景与目标.md)
- [02-三方项目对齐分析](02-三方项目对齐分析.md)
- [03-目标架构总览](03-目标架构总览.md)
- [04-领域事件驱动设计模式](04-领域事件驱动设计模式.md)（核心）
- [05-RBAC权限控制设计](05-RBAC权限控制设计.md)（核心）
- [06-分层落地详解](06-分层落地详解.md)
- [07-完整迁移路径](07-完整迁移路径.md)
- [08-数据迁移](08-数据迁移.md)
- [09-配置与部署](09-配置与部署.md)
- [10-风险清单与遗留技术债](10-风险清单与遗留技术债.md)
- [11-附录](11-附录.md)
- [12-8天工作流程](12-8天工作流程.md) ⭐

---

## 一图看懂（TL;DR）

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

## 三句话总结

1. **架构形态**：从 OKR 的"9 微服务 + RabbitMQ 跨进程事件"收敛为"单体 DDD + Spring 进程内领域事件"，跨服务 Feign 调用变成同进程方法调用，RabbitMQ 事件变成 `ApplicationEvent`（保留 MQ 作为未来拆分时的可选出口）。
2. **写法借鉴**：抄 ai-mcp-gateway 的**七模块分层 + 依赖方向 + Repository 端口反转 + `IXxxService`/`XxxService` 领域服务组织**；但它**没有实现领域事件**（`trigger/listener/` 是空目录，只有 `package-info` 注释提示方向），事件驱动部分需要我们基于 OKR 原有链路在 DDD 下重新落地。
3. **新增能力**：把 OKR"建了表但代码没启用"的 RBAC 五表（`sys_role`/`sys_permission`/`sys_user_role`/`sys_role_permission` + `data_scope`）完整启用，做成"认证(JWT) + 鉴权(perm_code 注解) + 数据权限(data_scope)"三层，鉴权放 trigger 层拦截器，不污染业务。
