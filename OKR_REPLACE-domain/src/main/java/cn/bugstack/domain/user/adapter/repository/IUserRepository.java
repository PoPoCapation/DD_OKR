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

    List<String> queryPermissionCodesByUserId(Long userId);

    SystemUserVO queryUserByAccount(String account);

    String queryUserDataScope(Long userId);

    List<Long> queryDescendantDeptIds(Long deptId);

    List<Long> queryVisibleUserIds(Long userId);

    /** 查询当前用户可编辑的用户ID（自己 + 全部下级递归，不含上级） */
    List<Long> queryEditableUserIds(Long userId);
}
