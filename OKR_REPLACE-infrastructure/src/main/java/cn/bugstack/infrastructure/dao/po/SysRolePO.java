package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 角色表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysRolePO {

    /** 角色ID */
    private Long id;
    /** 组织/租户ID，单组织系统默认1 */
    private Long orgId;
    /** 角色编码，唯一，用于程序判断，如 admin、dept_admin */
    private String roleCode;
    /** 角色名称，展示用 */
    private String roleName;
    /** 数据范围：all全部, dept本部门, dept_and_below本部门及下级, self仅本人 */
    private String dataScope;
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
