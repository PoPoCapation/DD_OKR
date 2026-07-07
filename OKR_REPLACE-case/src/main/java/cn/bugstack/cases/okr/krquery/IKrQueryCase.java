package cn.bugstack.cases.okr.krquery;

import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;

import java.util.List;

/**
 * 查询 KR 用例编排（校验对 O 可见 + 查 KR）
 */
public interface IKrQueryCase {
    List<OkrKeyResultVO> queryKeyResultList(Long currentUserId, Long objectiveId);
}
