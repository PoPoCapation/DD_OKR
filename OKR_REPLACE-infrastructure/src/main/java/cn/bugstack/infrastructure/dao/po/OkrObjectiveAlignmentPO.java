package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * OKR目标对齐关系表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrObjectiveAlignmentPO {

    /** 对齐关系ID */
    private Long id;
    /** 当前O ID，即发起对齐的O */
    private Long objectiveId;
    /** 对齐的上级O ID */
    private Long alignedObjectiveId;
    /** 对齐的上级KR ID；为空表示只对齐到O */
    private Long alignedKrId;
    /** 对齐类型：upward向上对齐 */
    private String alignmentType;
    /** 状态：active有效，cancelled取消 */
    private String status;
    /** 创建人ID */
    private Long createdBy;
    /** 更新人ID */
    private Long updatedBy;
    /** 创建时间 */
    private Date createtime;
    /** 修改时间 */
    private Date updatetime;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
}
