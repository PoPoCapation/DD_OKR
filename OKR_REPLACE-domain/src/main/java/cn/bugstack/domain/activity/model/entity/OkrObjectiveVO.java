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
public class OkrObjectiveVO {
    /** O主键ID */
    private Long id;
    /** Objective名称 */
    private String objectiveName;
    /** 负责人用户ID */
    private Long ownerUserId;
    /** Objective所属部门ID */
    private Long departmentId;
    /** 所属OKR周期ID */
    private Long cycleId;
    /** 进度：0-100 */
    private BigDecimal progress;
    /** O状态：draft/ongoing/done/closed */
    private String status;
    /** 备注 */
    private String remark;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
