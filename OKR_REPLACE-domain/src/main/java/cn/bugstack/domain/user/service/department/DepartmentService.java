package cn.bugstack.domain.user.service.department;

import cn.bugstack.domain.user.adapter.repository.IDepartmentRepository;
import cn.bugstack.domain.user.model.entity.SystemDepartmentVO;
import cn.bugstack.domain.user.service.IDepartmentService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DepartmentService implements IDepartmentService {

    @Resource
    private IDepartmentRepository repository;

    @Override
    public void createDepartment(SystemDepartmentVO systemDepartmentVO) {
        log.info("开始创建Department: deptCode = {}", systemDepartmentVO.getDeptCode());
        boolean isCreate = repository.createDepartment(systemDepartmentVO);
        if (!isCreate) {
            throw new AppException(ResponseCode.DEPARTMENT_CREATE_FAIL.getCode(), ResponseCode.DEPARTMENT_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateDepartment(SystemDepartmentVO systemDepartmentVO) {
        log.info("开始更新Department: deptCode = {}", systemDepartmentVO.getDeptCode());
        boolean isUpdate = repository.updateDepartment(systemDepartmentVO);
        if (!isUpdate) {
            throw new AppException(ResponseCode.DEPARTMENT_UPDATE_FAIL.getCode(), ResponseCode.DEPARTMENT_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        log.info("开始删除Department: departmentId = {}", departmentId);
        boolean isDelete = repository.deleteDepartment(departmentId);
        if (!isDelete) {
            throw new AppException(ResponseCode.DEPARTMENT_DELETE_FAIL.getCode(), ResponseCode.DEPARTMENT_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public SystemDepartmentVO queryDepartmentByDepartmentId(Long departmentId) {
        log.info("开始查询Department: departmentId = {}", departmentId);
        SystemDepartmentVO departmentVO = repository.queryDepartmentByDepartmentId(departmentId);
        if (null == departmentVO) {
            throw new AppException(ResponseCode.DEPARTMENT_FIND_FAIL.getCode(), ResponseCode.DEPARTMENT_FIND_FAIL.getInfo());
        }
        return departmentVO;
    }
}
