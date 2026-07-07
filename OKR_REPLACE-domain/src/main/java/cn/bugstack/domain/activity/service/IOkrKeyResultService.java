package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.types.common.PageResult;

import java.util.List;

/**
 * KR（关键结果）Service
 */
public interface IOkrKeyResultService {

    /** 创建 KR（currentUserId 用于审计） */
    void createKeyResult(Long currentUserId, OkrKeyResultVO vo);

    /** 更新 KR（currentUserId 用于审计） */
    void updateKeyResult(Long currentUserId, OkrKeyResultVO vo);

    /** 删除 KR（逻辑删除，currentUserId 用于审计） */
    void deleteKeyResult(Long currentUserId, Long krId);

    /** 根据目标ID查询其下所有 KR（校验当前用户对该 O 的可见性） */
    List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long currentUserId, Long objectiveId);

    /** 根据目标ID分页查询其下 KR（校验当前用户对该 O 的可见性） */
    PageResult<OkrKeyResultVO> queryKeyResultPage(Long currentUserId, Long objectiveId, Integer page, Integer size);
}
