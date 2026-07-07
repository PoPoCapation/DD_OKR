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
public class OkrTaskResponseDTO {
    private Long id;
    private String taskName;
    private String status;
    private Long ownerUserId;
    private Long krId;
    private Long departmentId;
    private Integer priority;
    private Date deadline;
    private String remark;
    private Date createtime;
    private Date updatetime;
}
