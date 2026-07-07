package cn.bugstack.domain.activity.adapter.repository;

import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;

import java.util.List;

/**
 * KR Repository
 */
public interface IOkrKeyResultRepository {

    boolean createKeyResult(OkrKeyResultVO vo);

    boolean updateKeyResult(OkrKeyResultVO vo);

    boolean deleteKeyResult(Long krId);

    /** 根据ID查询单个 KR */
    OkrKeyResultVO queryKeyResultById(Long krId);

    List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long objectiveId);
}
