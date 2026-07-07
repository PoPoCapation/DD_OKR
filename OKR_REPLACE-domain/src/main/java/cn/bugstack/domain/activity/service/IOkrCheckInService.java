package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;
import cn.bugstack.domain.activity.model.entity.OkrCheckInVO;

import java.util.List;

public interface IOkrCheckInService {
    Long createCheckIn(Long currentUserId, Long objectiveId, Integer confidence, String summary, String risk, String blocker, String nextPlan, List<OkrCheckInItemVO> items);
    List<OkrCheckInVO> queryByObjectiveId(Long objectiveId);
    List<OkrCheckInItemVO> queryItems(Long checkInId);
}
