package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 角色与权限关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysRolePermissionPO {

    /** 关联主键ID */
    private Long id;
    /** 角色ID */
    private Long roleId;
    /** 权限ID */
    private Long permissionId;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
