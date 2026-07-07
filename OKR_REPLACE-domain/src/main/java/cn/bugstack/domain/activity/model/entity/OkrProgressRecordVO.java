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
public class OkrProgressRecordVO {
    private Long id;
    private String targetType;
    private Long targetId;
    private BigDecimal oldProgress;
    private BigDecimal newProgress;
    private String sourceType;
    private Long operatorId;
    private String remark;
    private Integer isDeleted;
    private Date createdAt;
}
