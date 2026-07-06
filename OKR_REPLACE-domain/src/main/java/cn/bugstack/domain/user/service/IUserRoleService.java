package cn.bugstack.domain.user.service;

import java.util.List;

public interface IUserRoleService {
    /** 设置用户角色（全量覆盖：以传入列表为准，移除多余的、新增缺失的） */
    void setUserRoles(Long userId, List<Long> roleIds);

    /** 为用户新增角色（已存在的角色自动去重） */
    void addUserRoles(Long userId, List<Long> roleIds);

    /** 移除用户的指定角色 */
    void removeUserRoles(Long userId, List<Long> roleIds);

    /** 查询用户的角色ID列表（用于编辑回显，不做角色状态过滤） */
    List<Long> queryRoleIdsByUserId(Long userId);

    /** 查询用户的角色编码列表（鉴权用，仅返回启用且未删除的角色） */
    List<String> queryRoleCodesByUserId(Long userId);

    /** 查询用户的权限编码列表（联三表，仅返回启用且未删除的权限，鉴权用） */
    List<String> queryPermissionCodesByUserId(Long userId);
}
