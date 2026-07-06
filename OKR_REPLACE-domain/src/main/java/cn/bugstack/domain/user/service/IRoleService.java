package cn.bugstack.domain.user.service;

import cn.bugstack.domain.user.model.entity.SystemRoleVO;

import java.util.List;

public interface IRoleService {
    /** 创建角色 */
    void createRole(SystemRoleVO systemRoleVO);

    /** 更新角色 */
    void updateRole(SystemRoleVO systemRoleVO);

    /** 根据角色ID删除角色（逻辑删除） */
    void deleteRole(Long roleId);

    /** 根据角色ID查询角色 */
    SystemRoleVO queryRoleByRoleId(Long roleId);

    /** 设置角色权限（全量覆盖：以传入列表为准，移除多余的、新增缺失的） */
    void setRolePermissions(Long roleId, List<Long> permissionIds);

    /** 为角色新增权限（已存在的自动去重） */
    void addRolePermissions(Long roleId, List<Long> permissionIds);

    /** 移除角色的指定权限 */
    void removeRolePermissions(Long roleId, List<Long> permissionIds);

    /** 查询角色的权限ID列表（用于编辑回显） */
    List<Long> queryPermissionIdsByRoleId(Long roleId);
}
