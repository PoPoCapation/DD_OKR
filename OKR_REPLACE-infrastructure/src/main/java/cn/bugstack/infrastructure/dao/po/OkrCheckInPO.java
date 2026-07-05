package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * OKR Check-in主表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrCheckInPO {

    /** 主键ID */
    private Long id;
    /** Check-in关联的Objective ID */
    private Long objectiveId;
    /** 周期ID */
    private Long cycleId;
    /** 提交人ID */
    private Long checkInUserId;
    /** 信心指数：1-5 */
    private Integer confidenceLevel;
    /** 本次进展总结 */
    private String summary;
    /** 风险说明 */
    private String risk;
    /** 阻塞说明 */
    private String blocker;
    /** 下一步计划 */
    private String nextPlan;
    /** 异步应用状态：PENDING/APPLIED/FAILED */
    private String applyStatus;
    /** 异步应用结果说明 */
    private String applyMessage;
    /** 提交时间 */
    private Date submittedAt;
    /** 异步应用完成时间 */
    private Date appliedAt;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
