package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OkrTaskRequestDTO {
    /** 任务ID（更新时必填） */
    private Long id;
    private String taskName;
    /** todo/ongoing/done/cancel */
    private String status;
    private Long ownerUserId;
    /** 关联KR ID */
    private Long krId;
    private Long departmentId;
    /** 1低 2中 3高 */
    private Integer priority;
    private Date deadline;
    private String remark;
}
