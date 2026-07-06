package cn.bugstack.domain.user.service.user;

import cn.bugstack.domain.user.adapter.repository.IUserRepository;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.common.PasswordEncoder;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService implements IUserService {
    @Resource
    private IUserRepository repository;

    @Override
    public void createUser(SystemUserVO systemUserVO) {
        log.info("开始创建User: UserUnionAccount = {}" , systemUserVO.getAccount());
        // 密码 BCrypt 加密后存储，登录时用 PasswordEncoder.matches 校验
        systemUserVO.setPassword(PasswordEncoder.encode(systemUserVO.getPassword()));
        boolean isCreate = repository.createUser(systemUserVO);
        if (!isCreate) {
            throw new AppException(ResponseCode.USER_CREATE_FAIL.getCode(), ResponseCode.USER_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateUser(SystemUserVO systemUserVO) {
        log.info("开始更新User: UserUnionAccount = {}" , systemUserVO.getAccount());
        boolean isUpdate = repository.updateUser(systemUserVO);
        if (!isUpdate) {
            throw new AppException(ResponseCode.USER_UPDATE_FAIL.getCode(), ResponseCode.USER_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("开始删除User: UserId = {}" , userId);
        boolean isDelete = repository.deleteUser(userId);
        if (!isDelete) {
            throw new AppException(ResponseCode.USER_DELETE_FAIL.getCode(), ResponseCode.USER_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public SystemUserVO queryUserByUserId(Long userId) {
        log.info("开始查询User: UserId = {}" , userId);
        SystemUserVO userVO = repository.queryUserByUserId(userId);
        if (null == userVO) {
            throw new AppException(ResponseCode.USER_FIND_FAIL.getCode(), ResponseCode.USER_FIND_FAIL.getInfo());
        }
        return userVO;
    }

    @Override
    public SystemUserVO queryUserByAccount(String account) {
        log.info("开始查询User: Account = {}" , account);
        SystemUserVO userVO = repository.queryUserByAccount(account);
        if (null == userVO) {
            throw new AppException(ResponseCode.USER_FIND_FAIL.getCode(), ResponseCode.USER_FIND_FAIL.getInfo());
        }
        return userVO;
    }
}
