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
public class OkrObjectiveResponseDTO {
    private Long id;
    private String objectiveName;
    private Long ownerUserId;
    private Long departmentId;
    private Long cycleId;
    private BigDecimal progress;
    private String status;
    private String remark;
    private Date createtime;
    private Date updatetime;
}
