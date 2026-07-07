package cn.bugstack.domain.activity.adapter.repository;

import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;
import cn.bugstack.domain.activity.model.entity.OkrCheckInVO;

import java.util.List;

public interface IOkrCheckInRepository {
    Long createCheckIn(OkrCheckInVO vo);
    List<OkrCheckInVO> queryByObjectiveId(Long objectiveId);
    void insertItem(OkrCheckInItemVO item);
    List<OkrCheckInItemVO> queryItemsByCheckInId(Long checkInId);
}
