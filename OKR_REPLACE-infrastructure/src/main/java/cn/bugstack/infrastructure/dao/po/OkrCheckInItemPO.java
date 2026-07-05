package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * OKR Check-in明细表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrCheckInItemPO {

    /** 主键ID */
    private Long id;
    /** Check-in主表ID */
    private Long checkInId;
    /** KR ID */
    private Long krId;
    /** 提交时KR旧完成度 */
    private BigDecimal oldCompletionRate;
    /** 提交的新完成度 */
    private BigDecimal newCompletionRate;
    /** 完成度变化量 */
    private BigDecimal progressDelta;
    /** KR进展说明 */
    private String remark;
    /** 异步应用状态：PENDING/APPLIED/FAILED */
    private String applyStatus;
    /** 异步应用结果说明 */
    private String applyMessage;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
