package cn.bugstack.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    /** KR 名称（创建时必填） */
    @NotBlank(message = "KR名称不能为空")
    private String krName;
    private Integer sortOrder;
    /** 权重，需 ≥ 0 */
    @PositiveOrZero(message = "权重需为非负数")
    private BigDecimal weight;
    /** 完成度，需 ≥ 0 */
    @PositiveOrZero(message = "完成度需为非负数")
    private BigDecimal completionRate;
    /** 关联目标ID（创建时必填） */
    @NotNull(message = "关联目标ID不能为空")
    private Long objectiveId;
    private Date deadline;
    /** todo/ongoing/done */
    private String status;
    private String remark;
}
