package cn.bugstack.domain.user.service;

import cn.bugstack.domain.user.model.entity.SystemDepartmentVO;

public interface IDepartmentService {
    /** 创建部门 */
    void createDepartment(SystemDepartmentVO systemDepartmentVO);

    /** 更新部门 */
    void updateDepartment(SystemDepartmentVO systemDepartmentVO);

    /** 根据部门ID删除部门（逻辑删除） */
    void deleteDepartment(Long departmentId);

    /** 根据部门ID查询部门 */
    SystemDepartmentVO queryDepartmentByDepartmentId(Long departmentId);
}
