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
public class OkrKeyResultResponseDTO {
    private Long id;
    private String krName;
    private Integer sortOrder;
    private BigDecimal weight;
    private BigDecimal completionRate;
    private Long objectiveId;
    private Date deadline;
    private String status;
    private String remark;
    private Date createtime;
    private Date updatetime;
}
