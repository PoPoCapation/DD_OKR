package cn.bugstack.domain.user.service;


import cn.bugstack.domain.user.model.entity.SystemUserVO;

public interface IUserService {
    void createUser(SystemUserVO systemUserVO);
    void updateUser(SystemUserVO systemUserVO);
    void deleteUser(Long userId);
    SystemUserVO queryUserByUserId(Long userId);
}
