package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 部门表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysDepartmentPO {

    /** 部门ID */
    private Long id;
    /** 组织/租户ID，单组织系统默认1 */
    private Long orgId;
    /** 上级部门ID，0表示根部门 */
    private Long parentId;
    /** 部门名称 */
    private String deptName;
    /** 部门编码 */
    private String deptCode;
    /** 部门负责人用户ID */
    private Long leaderUserId;
    /** 排序值，越小越靠前 */
    private Integer sortOrder;
    /** 状态：1启用，0禁用 */
    private Integer status;
    /** 备注 */
    private String remark;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
