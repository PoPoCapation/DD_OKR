package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysRolePermissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /** 根据角色ID查询其权限ID列表（用于关联管理与 diff） */
    List<Long> queryPermissionIdsByRoleId(Long roleId);

    /** 批量删除角色指定权限关联（permissionIds 为空时不应调用） */
    int deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /** 批量新增角色权限关联（list 为空时不应调用） */
    int insertBatch(@Param("list") List<SysRolePermissionPO> list);
}
