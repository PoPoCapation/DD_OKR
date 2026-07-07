package cn.bugstack.cases.okr.checkin.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CheckInCaseFactory {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckInContext {
        private Long currentUserId;
        private Long objectiveId;
        private Integer confidence;
        private String summary;
        private String risk;
        private String blocker;
        private String nextPlan;
        private List<cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO> items;
        private Long checkInId;
    }
}
