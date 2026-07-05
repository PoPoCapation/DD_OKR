package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * OKR进度记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrProgressRecordPO {

    /** 主键ID */
    private Long id;
    /** OBJECTIVE/KR/TASK */
    private String targetType;
    /** 目标ID */
    private Long targetId;
    /** 变更前进度 */
    private BigDecimal oldProgress;
    /** 变更后进度 */
    private BigDecimal newProgress;
    /** MANUAL/TASK_DERIVATION/SYSTEM */
    private String sourceType;
    /** 操作人ID */
    private Long operatorId;
    /** 备注 */
    private String remark;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createdAt;
}
