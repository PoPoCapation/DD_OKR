package cn.bugstack.domain.user.adapter.repository;

import cn.bugstack.domain.user.model.entity.SystemUserVO;

import java.util.List;

public interface IUserRepository {

    boolean createUser(SystemUserVO systemUserVO);

    boolean updateUser(SystemUserVO systemUserVO);

    boolean deleteUser(Long userId);

    SystemUserVO queryUserByUserId(Long userId);

    boolean setUserRoles(Long userId, List<Long> roleIds);

    boolean addUserRoles(Long userId, List<Long> roleIds);

    boolean removeUserRoles(Long userId, List<Long> roleIds);

    List<Long> queryRoleIdsByUserId(Long userId);

    List<String> queryRoleCodesByUserId(Long userId);

    /** 查询用户的权限编码列表（联三表，仅返回启用且未删除的权限，鉴权用） */
    List<String> queryPermissionCodesByUserId(Long userId);

    /** 根据登录账号查询用户（登录用） */
    SystemUserVO queryUserByAccount(String account);

    /** 查询用户的最宽数据范围（取用户所有角色 data_scope 中范围最大的） */
    String queryUserDataScope(Long userId);

    /** 查询本部门及所有下级部门ID（含自身，dept_and_below 数据权限用） */
    List<Long> queryDescendantDeptIds(Long deptId);

    /** 查询当前用户汇报关系下可见的用户ID（自己 + 直属上级 + 全部下级递归） */
    List<Long> queryVisibleUserIds(Long userId);
}