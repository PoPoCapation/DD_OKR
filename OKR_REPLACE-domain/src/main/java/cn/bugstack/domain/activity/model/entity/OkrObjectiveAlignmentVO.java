package cn.bugstack.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OkrObjectiveAlignmentVO {
    private Long id;
    private Long objectiveId;
    private Long alignedObjectiveId;
    private Long alignedKrId;
    private String alignmentType;
    private String status;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
    private Date createtime;
    private Date updatetime;
}
