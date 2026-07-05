package cn.bugstack.domain.user.adapter.repository;

import cn.bugstack.domain.user.model.entity.SystemUserVO;

public interface IUserRepository {

    boolean createUser(SystemUserVO systemUserVO);

    boolean updateUser(SystemUserVO systemUserVO);

    boolean deleteUser(Long userId);

    SystemUserVO queryUserByUserId(Long userId);
}
