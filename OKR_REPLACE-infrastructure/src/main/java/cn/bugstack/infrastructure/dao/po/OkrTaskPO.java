package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Task表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrTaskPO {

    /** 任务主键ID */
    private Long id;
    /** Task名称 */
    private String taskName;
    /** 任务状态：todo/ongoing/done/cancel */
    private String status;
    /** 归属人用户ID */
    private Long ownerUserId;
    /** 关联KR ID */
    private Long krId;
    /** 归属部门ID */
    private Long departmentId;
    /** 优先级：1低，2中，3高 */
    private Integer priority;
    /** 任务截止时间 */
    private Date deadline;
    /** 备注 */
    private String remark;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
