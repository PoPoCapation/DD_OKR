package cn.bugstack.domain.user.service;

import cn.bugstack.domain.user.model.entity.SystemPermissionVO;

public interface IPermissionService {
    /** 创建权限 */
    void createPermission(SystemPermissionVO systemPermissionVO);

    /** 更新权限 */
    void updatePermission(SystemPermissionVO systemPermissionVO);

    /** 根据权限ID删除权限（逻辑删除） */
    void deletePermission(Long permissionId);

    /** 根据权限ID查询权限 */
    SystemPermissionVO queryPermissionByPermissionId(Long permissionId);
}
