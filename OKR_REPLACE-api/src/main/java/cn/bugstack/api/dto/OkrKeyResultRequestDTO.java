package cn.bugstack.api.dto;

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
public class OkrKeyResultRequestDTO {
    /** KR ID（更新时必填） */
    private Long id;
    private String krName;
    private Integer sortOrder;
    private BigDecimal weight;
    private BigDecimal completionRate;
    /** 关联目标ID */
    private Long objectiveId;
    private Date deadline;
    /** todo/ongoing/done */
    private String status;
    private String remark;
}
