package cn.bugstack.cases.okr.checkin;

import cn.bugstack.cases.okr.checkin.factory.CheckInCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;
import cn.bugstack.domain.activity.service.IOkrCheckInService;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CheckInCaseService implements ICheckInCase {

    @Resource
    private IOkrCheckInService checkInService;

    @Override
    public Long createCheckIn(Long currentUserId, Long objectiveId, Integer confidence,
                              String summary, String risk, String blocker, String nextPlan,
                              List<OkrCheckInItemVO> items) {
        log.info("Case编排-创建复盘: objectiveId={}, userId={}", objectiveId, currentUserId);
        try {
            return checkInService.createCheckIn(currentUserId, objectiveId, confidence, summary, risk, blocker, nextPlan, items);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
