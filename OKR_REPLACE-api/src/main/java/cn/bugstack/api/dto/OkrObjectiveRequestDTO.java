package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OkrObjectiveRequestDTO {
    /** 目标ID（更新时必填） */
    private Long id;
    /** Objective名称 */
    private String objectiveName;
    /** 所属OKR周期ID */
    private Long cycleId;
    /** 进度 0-100 */
    private BigDecimal progress;
    /** 状态：draft/ongoing/done/closed */
    private String status;
    /** 备注 */
    private String remark;
}
