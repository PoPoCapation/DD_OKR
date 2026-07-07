package cn.bugstack.cases.okr.checkin;

import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;

import java.util.List;

/**
 * 创建复盘用例编排
 */
public interface ICheckInCase {
    Long createCheckIn(Long currentUserId, Long objectiveId, Integer confidence,
                       String summary, String risk, String blocker, String nextPlan,
                       List<OkrCheckInItemVO> items);
}
