package cn.bugstack.domain.user.adapter.repository;

import cn.bugstack.domain.user.model.entity.SystemPermissionVO;

public interface IPermissionRepository {
    boolean createPermission(SystemPermissionVO systemPermissionVO);

    boolean updatePermission(SystemPermissionVO systemPermissionVO);

    boolean deletePermission(Long permissionId);

    SystemPermissionVO queryPermissionByPermissionId(Long permissionId);
}
