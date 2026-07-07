package cn.bugstack.cases.okr.create;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;

/**
 * 创建目标用例编排（case 层）：设 owner/dept → 创建
 */
public interface ICreateObjectiveCase {

    Boolean createObjective(Long currentUserId, OkrObjectiveVO vo);
}
