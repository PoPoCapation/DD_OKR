package cn.bugstack.domain.activity.service;

import java.math.BigDecimal;

/**
 * OKR 审计服务
 * <p>
 * 统一封装「进度记录（okr_progress_record）」与「操作日志（okr_operation_log）」两类审计写入。
 * 所有关键业务变更（KR / Task / Objective / Alignment / Check-in 的增删改）都在其
 * {@code @Transactional} 业务方法内调用本服务，保证审计与业务在同一事务内提交/回滚，
 * 从而满足「业务成功但日志丢失」的零容忍要求。
 */
public interface IOkrAuditService {

    /**
     * 记录一次进度变更流水。
     *
     * @param targetType    目标类型 {@link cn.bugstack.types.enums.ProgressTargetType#code()}
     * @param targetId      目标ID
     * @param oldProgress   变更前进度（新增时为 null）
     * @param newProgress   变更后进度（删除时为 null）
     * @param sourceType    变更来源（KR_CREATE / KR_UPDATE / KR_DELETE / TASK_UPDATE / CHECK_IN / OBJECTIVE_RECALC）
     * @param operatorId    操作人ID
     * @param remark        备注
     */
    void recordProgress(String targetType, Long targetId, BigDecimal oldProgress, BigDecimal newProgress,
                        String sourceType, Long operatorId, String remark);

    /**
     * 记录一次操作日志（含前后 JSON 快照）。
     *
     * @param serviceName   模块/服务名（如 OkrKeyResultService）
     * @param resourceType  资源类型（OBJECTIVE / KR / TASK / ALIGNMENT / CHECK_IN）
     * @param resourceId     资源ID
     * @param action        动作（CREATE / UPDATE / DELETE / LINK / UNLINK）
     * @param operatorId    操作人ID
     * @param before        变更前对象（序列化为 before_json，可为 null）
     * @param after         变更后对象（序列化为 after_json，可为 null）
     */
    void recordOperation(String serviceName, String resourceType, Long resourceId, String action,
                         Long operatorId, Object before, Object after);
}
