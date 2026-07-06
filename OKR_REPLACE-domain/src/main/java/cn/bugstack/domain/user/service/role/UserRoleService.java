package cn.bugstack.domain.user.service.role;

import cn.bugstack.domain.user.adapter.repository.IUserRepository;
import cn.bugstack.domain.user.service.IUserRoleService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserRoleService implements IUserRoleService {

    @Resource
    private IUserRepository repository;

    @Override
    @Transactional
    public void setUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        log.info("开始设置用户角色: userId = {}, roleIds = {}", userId, roleIds);
        boolean isSet = repository.setUserRoles(userId, roleIds);
        if (!isSet) {
            throw new AppException(ResponseCode.USER_ROLE_SET_FAIL.getCode(), ResponseCode.USER_ROLE_SET_FAIL.getInfo());
        }
    }

    @Override
    @Transactional
    public void addUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        log.info("开始新增用户角色: userId = {}, roleIds = {}", userId, roleIds);
        boolean isAdd = repository.addUserRoles(userId, roleIds);
        if (!isAdd) {
            throw new AppException(ResponseCode.USER_ROLE_ADD_FAIL.getCode(), ResponseCode.USER_ROLE_ADD_FAIL.getInfo());
        }
    }

    @Override
    @Transactional
    public void removeUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        log.info("开始移除用户角色: userId = {}, roleIds = {}", userId, roleIds);
        boolean isRemove = repository.removeUserRoles(userId, roleIds);
        if (!isRemove) {
            throw new AppException(ResponseCode.USER_ROLE_REMOVE_FAIL.getCode(), ResponseCode.USER_ROLE_REMOVE_FAIL.getInfo());
        }
    }

    @Override
    public List<Long> queryRoleIdsByUserId(Long userId) {
        if (null == userId) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        log.info("开始查询用户角色ID列表: userId = {}", userId);
        return repository.queryRoleIdsByUserId(userId);
    }

    @Override
    public List<String> queryRoleCodesByUserId(Long userId) {
        if (null == userId) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        log.info("开始查询用户角色编码列表: userId = {}", userId);
        return repository.queryRoleCodesByUserId(userId);
    }

    @Override
    public List<String> queryPermissionCodesByUserId(Long userId) {
        if (null == userId) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        log.info("开始查询用户权限编码列表: userId = {}", userId);
        return repository.queryPermissionCodesByUserId(userId);
    }
}