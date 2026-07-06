package cn.bugstack.domain.user.adapter.repository;

import cn.bugstack.domain.user.model.entity.SystemDepartmentVO;

public interface IDepartmentRepository {
    boolean createDepartment(SystemDepartmentVO systemDepartmentVO);

    boolean updateDepartment(SystemDepartmentVO systemDepartmentVO);

    boolean deleteDepartment(Long departmentId);

    SystemDepartmentVO queryDepartmentByDepartmentId(Long departmentId);
}
