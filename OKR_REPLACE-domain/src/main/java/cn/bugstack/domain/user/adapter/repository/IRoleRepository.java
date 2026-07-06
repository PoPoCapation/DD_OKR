package cn.bugstack.domain.user.adapter.repository;

import cn.bugstack.domain.user.model.entity.SystemRoleVO;

import java.util.List;

public interface IRoleRepository {

    boolean createRole(SystemRoleVO systemRoleVO);

    boolean updateRole(SystemRoleVO systemRoleVO);

    boolean deleteRole(Long roleId);

    SystemRoleVO queryRoleByRoleId(Long roleId);

    boolean setRolePermissions(Long roleId, List<Long> permissionIds);

    boolean addRolePermissions(Long roleId, List<Long> permissionIds);

    boolean removeRolePermissions(Long roleId, List<Long> permissionIds);

    List<Long> queryPermissionIdsByRoleId(Long roleId);
}
