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
public class OkrCheckInItemVO {
    private Long id;
    private Long checkInId;
    private Long krId;
    private BigDecimal oldCompletionRate;
    private BigDecimal newCompletionRate;
    private BigDecimal progressDelta;
    private String remark;
    private String applyStatus;
    private Integer isDeleted;
    private Date createtime;
    private Date updatetime;
}
