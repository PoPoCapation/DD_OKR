package cn.bugstack.domain.user.service;

import cn.bugstack.domain.user.model.entity.SystemUserVO;

import java.util.List;

public interface IUserService {
    /** 创建用户 */
    void createUser(SystemUserVO systemUserVO);

    /** 更新用户 */
    void updateUser(SystemUserVO systemUserVO);

    /** 根据用户ID删除用户（逻辑删除） */
    void deleteUser(Long userId);

    /** 根据用户ID查询用户 */
    SystemUserVO queryUserByUserId(Long userId);

    /** 根据登录账号查询用户（登录用） */
    SystemUserVO queryUserByAccount(String account);

    /** 查询用户的最宽数据范围（all/dept_and_below/dept/self），用于数据权限过滤 */
    String queryUserDataScope(Long userId);

    /** 查询本部门及所有下级部门ID（含自身，dept_and_below 数据权限用） */
    List<Long> queryDescendantDeptIds(Long deptId);

    /** 查询当前用户汇报关系下可见的用户ID（自己 + 上级 + 下级递归） */
    List<Long> queryVisibleUserIds(Long userId);

    /** 查询当前用户可编辑的用户ID（自己 + 全部下级递归，不含上级） */
    List<Long> queryEditableUserIds(Long userId);
}
