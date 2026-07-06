package cn.bugstack.domain.user.service.permission;

import cn.bugstack.domain.user.adapter.repository.IPermissionRepository;
import cn.bugstack.domain.user.model.entity.SystemPermissionVO;
import cn.bugstack.domain.user.service.IPermissionService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PermissionService implements IPermissionService {

    @Resource
    private IPermissionRepository repository;

    @Override
    public void createPermission(SystemPermissionVO systemPermissionVO) {
        log.info("开始创建Permission: permCode = {}", systemPermissionVO.getPermCode());
        boolean isCreate = repository.createPermission(systemPermissionVO);
        if (!isCreate) {
            throw new AppException(ResponseCode.PERMISSION_CREATE_FAIL.getCode(), ResponseCode.PERMISSION_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updatePermission(SystemPermissionVO systemPermissionVO) {
        log.info("开始更新Permission: permCode = {}", systemPermissionVO.getPermCode());
        boolean isUpdate = repository.updatePermission(systemPermissionVO);
        if (!isUpdate) {
            throw new AppException(ResponseCode.PERMISSION_UPDATE_FAIL.getCode(), ResponseCode.PERMISSION_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deletePermission(Long permissionId) {
        log.info("开始删除Permission: permissionId = {}", permissionId);
        boolean isDelete = repository.deletePermission(permissionId);
        if (!isDelete) {
            throw new AppException(ResponseCode.PERMISSION_DELETE_FAIL.getCode(), ResponseCode.PERMISSION_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public SystemPermissionVO queryPermissionByPermissionId(Long permissionId) {
        log.info("开始查询Permission: permissionId = {}", permissionId);
        SystemPermissionVO permissionVO = repository.queryPermissionByPermissionId(permissionId);
        if (null == permissionVO) {
            throw new AppException(ResponseCode.PERMISSION_FIND_FAIL.getCode(), ResponseCode.PERMISSION_FIND_FAIL.getInfo());
        }
        return permissionVO;
    }
}
