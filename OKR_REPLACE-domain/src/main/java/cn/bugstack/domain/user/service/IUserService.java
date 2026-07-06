package cn.bugstack.domain.user.service;


import cn.bugstack.domain.user.model.entity.SystemUserVO;

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
}
