package cn.bugstack.cases.okr.query;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;

import java.util.List;

/**
 * 查询目标用例编排（case 层）：算可见用户 → 查目标列表
 */
public interface IQueryObjectiveCase {

    List<OkrObjectiveVO> queryObjectiveList(Long currentUserId);
}
