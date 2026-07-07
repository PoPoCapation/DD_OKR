package cn.bugstack.cases.okr.krcreate;

import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;

/**
 * 创建 KR 用例编排
 */
public interface IKrCreateCase {
    Boolean createKeyResult(Long currentUserId, OkrKeyResultVO vo);
}
