package cn.bugstack.domain.user.service.role;

import cn.bugstack.domain.user.adapter.repository.IRoleRepository;
import cn.bugstack.domain.user.model.entity.SystemRoleVO;
import cn.bugstack.domain.user.service.IRoleService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RoleService implements IRoleService {

    @Resource
    private IRoleRepository repository;

    @Override
    public void createRole(SystemRoleVO systemRoleVO) {
        log.info("开始创建Role: roleCode = {}", systemRoleVO.getRoleCode());
        boolean isCreate = repository.createRole(systemRoleVO);
        if (!isCreate) {
            throw new AppException(ResponseCode.ROLE_CREATE_FAIL.getCode(), ResponseCode.ROLE_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateRole(SystemRoleVO systemRoleVO) {
        log.info("开始更新Role: roleCode = {}", systemRoleVO.getRoleCode());
        boolean isUpdate = repository.updateRole(systemRoleVO);
        if (!isUpdate) {
            throw new AppException(ResponseCode.ROLE_UPDATE_FAIL.getCode(), ResponseCode.ROLE_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteRole(Long roleId) {
        log.info("开始删除Role: roleId = {}", roleId);
        boolean isDelete = repository.deleteRole(roleId);
        if (!isDelete) {
            throw new AppException(ResponseCode.ROLE_DELETE_FAIL.getCode(), ResponseCode.ROLE_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public SystemRoleVO queryRoleByRoleId(Long roleId) {
        log.info("开始查询Role: roleId = {}", roleId);
        SystemRoleVO roleVO = repository.queryRoleByRoleId(roleId);
        if (null == roleVO) {
            throw new AppException(ResponseCode.ROLE_FIND_FAIL.getCode(), ResponseCode.ROLE_FIND_FAIL.getInfo());
        }
        return roleVO;
    }

    @Override
    @Transactional
    public void setRolePermissions(Long roleId, List<Long> permissionIds) {
        log.info("开始设置角色权限: roleId = {}, permissionIds = {}", roleId, permissionIds);
        boolean isSet = repository.setRolePermissions(roleId, permissionIds);
        if (!isSet) {
            throw new AppException(ResponseCode.ROLE_PERMISSION_SET_FAIL.getCode(), ResponseCode.ROLE_PERMISSION_SET_FAIL.getInfo());
        }
    }

    @Override
    @Transactional
    public void addRolePermissions(Long roleId, List<Long> permissionIds) {
        log.info("开始新增角色权限: roleId = {}, permissionIds = {}", roleId, permissionIds);
        boolean isAdd = repository.addRolePermissions(roleId, permissionIds);
        if (!isAdd) {
            throw new AppException(ResponseCode.ROLE_PERMISSION_ADD_FAIL.getCode(), ResponseCode.ROLE_PERMISSION_ADD_FAIL.getInfo());
        }
    }

    @Override
    @Transactional
    public void removeRolePermissions(Long roleId, List<Long> permissionIds) {
        log.info("开始移除角色权限: roleId = {}, permissionIds = {}", roleId, permissionIds);
        boolean isRemove = repository.removeRolePermissions(roleId, permissionIds);
        if (!isRemove) {
            throw new AppException(ResponseCode.ROLE_PERMISSION_REMOVE_FAIL.getCode(), ResponseCode.ROLE_PERMISSION_REMOVE_FAIL.getInfo());
        }
    }

    @Override
    public List<Long> queryPermissionIdsByRoleId(Long roleId) {
        log.info("开始查询角色权限ID列表: roleId = {}", roleId);
        return repository.queryPermissionIdsByRoleId(roleId);
    }
}
