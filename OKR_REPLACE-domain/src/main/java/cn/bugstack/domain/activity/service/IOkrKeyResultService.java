package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;

import java.util.List;

/**
 * KR（关键结果）Service
 */
public interface IOkrKeyResultService {

    /** 创建 KR */
    void createKeyResult(OkrKeyResultVO vo);

    /** 更新 KR */
    void updateKeyResult(OkrKeyResultVO vo);

    /** 删除 KR（逻辑删除） */
    void deleteKeyResult(Long krId);

    /** 根据目标ID查询其下所有 KR（校验当前用户对该 O 的可见性） */
    List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long currentUserId, Long objectiveId);
}
