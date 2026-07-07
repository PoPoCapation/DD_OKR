package cn.bugstack.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OkrCheckInVO {
    private Long id;
    private Long objectiveId;
    private Long cycleId;
    private Long checkInUserId;
    private Integer confidenceLevel;
    private String summary;
    private String risk;
    private String blocker;
    private String nextPlan;
    private Date submittedAt;
    private Date createtime;
    private Date updatetime;
}
