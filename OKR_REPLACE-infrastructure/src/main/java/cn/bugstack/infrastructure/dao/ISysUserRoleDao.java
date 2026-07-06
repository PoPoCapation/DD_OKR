package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysUserRolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户与角色关联表 DAO
 */
@Mapper
public interface ISysUserRoleDao {

    /** 新增用户角色关联 */
    int insert(SysUserRolePO po);

    /** 根据ID查询用户角色关联 */
    SysUserRolePO queryById(Long id);

    /** 根据ID更新用户角色关联 */
    int update(SysUserRolePO po);

    /** 根据ID物理删除用户角色关联 */
    int delete(Long id);

    /** 根据用户ID查询其角色编码列表（关联 sys_role，仅返回启用且未删除的角色） */
    List<String> queryRoleCodesByUserId(Long userId);

    /** 根据用户ID查询其角色ID列表（不做角色状态过滤，用于关联管理与 diff） */
    List<Long> queryRoleIdsByUserId(Long userId);

    /** 批量删除用户指定角色关联（roleIds 为空时不应调用） */
    int deleteByUserIdAndRoleIds(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /** 批量新增用户角色关联（list 为空时不应调用） */
    int insertBatch(@Param("list") List<SysUserRolePO> list);

    /** 根据用户ID查询其权限编码列表（联 user_role + role_permission + permission，仅返回启用且未删除的权限） */
    List<String> queryPermissionCodesByUserId(Long userId);
}