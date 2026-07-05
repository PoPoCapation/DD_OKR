package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysRolePermissionPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与权限关联表 DAO
 */
@Mapper
public interface ISysRolePermissionDao {

    /** 新增角色权限关联 */
    int insert(SysRolePermissionPO po);

    /** 根据ID查询角色权限关联 */
    SysRolePermissionPO queryById(Long id);

    /** 根据ID更新角色权限关联 */
    int update(SysRolePermissionPO po);

    /** 根据ID物理删除角色权限关联 */
    int delete(Long id);
}
