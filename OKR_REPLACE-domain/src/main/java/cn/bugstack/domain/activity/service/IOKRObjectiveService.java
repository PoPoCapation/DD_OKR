package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;

import java.util.List;

/**
 * OKR 目标 Service
 */
public interface IOKRObjectiveService {

    /** 创建目标（ownerUserId/departmentId 取当前用户） */
    void createObjective(Long currentUserId, OkrObjectiveVO vo);

    /** 更新目标（校验数据权限） */
    void updateObjective(Long currentUserId, OkrObjectiveVO vo);

    /** 删除目标（校验数据权限） */
    void deleteObjective(Long currentUserId, Long objectiveId);

    /** 查询目标列表（按当前用户数据权限过滤） */
    List<OkrObjectiveVO> queryObjectiveList(Long currentUserId);

    /**
     * 重算目标进度（按 KR 加权汇总：Σ(rate×weight) / Σ(weight)）。
     * <p>
     * 重算后会写一条 OBJECTIVE 维度的进度流水（old→new），sourceType / operatorId 用于追溯变更来源与操作人。
     *
     * @param objectiveId 目标ID
     * @param operatorId  操作人ID（触发本次重算的用户）
     * @param sourceType  变更来源（KR_CREATE / KR_UPDATE / KR_DELETE / CHECK_IN / OBJECTIVE_RECALC）
     */
    void recalculateObjectiveProgress(Long objectiveId, Long operatorId, String sourceType);
}
