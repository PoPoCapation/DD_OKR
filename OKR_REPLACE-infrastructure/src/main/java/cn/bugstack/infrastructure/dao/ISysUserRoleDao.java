package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysUserRolePO;
import org.apache.ibatis.annotations.Mapper;

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
}
