package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.user.adapter.repository.IDepartmentRepository;
import cn.bugstack.domain.user.model.entity.SystemDepartmentVO;
import cn.bugstack.infrastructure.dao.ISysDepartmentDao;
import cn.bugstack.infrastructure.dao.po.SysDepartmentPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentRepository implements IDepartmentRepository {
    @Resource
    private ISysDepartmentDao sysDepartmentDao;

    @Override
    public boolean createDepartment(SystemDepartmentVO systemDepartmentVO) {
        SysDepartmentPO po = SysDepartmentPO.builder()
                .orgId(systemDepartmentVO.getOrgId())
                .parentId(systemDepartmentVO.getParentId())
                .deptName(systemDepartmentVO.getDeptName())
                .deptCode(systemDepartmentVO.getDeptCode())
                .leaderUserId(systemDepartmentVO.getLeaderUserId())
                .sortOrder(systemDepartmentVO.getSortOrder())
                .status(systemDepartmentVO.getStatus())
                .remark(systemDepartmentVO.getRemark())
                .isDeleted(systemDepartmentVO.getIsDeleted())
                .createtime(systemDepartmentVO.getCreatetime())
                .updatetime(systemDepartmentVO.getUpdatetime())
                .build();
        int insert = sysDepartmentDao.insert(po);
        return insert == 1;
    }

    @Override
    public boolean updateDepartment(SystemDepartmentVO systemDepartmentVO) {
        SysDepartmentPO po = SysDepartmentPO.builder()
                .id(systemDepartmentVO.getId())
                .orgId(systemDepartmentVO.getOrgId())
                .parentId(systemDepartmentVO.getParentId())
                .deptName(systemDepartmentVO.getDeptName())
                .deptCode(systemDepartmentVO.getDeptCode())
                .leaderUserId(systemDepartmentVO.getLeaderUserId())
                .sortOrder(systemDepartmentVO.getSortOrder())
                .status(systemDepartmentVO.getStatus())
                .remark(systemDepartmentVO.getRemark())
                .isDeleted(systemDepartmentVO.getIsDeleted())
                .createtime(systemDepartmentVO.getCreatetime())
                .updatetime(systemDepartmentVO.getUpdatetime())
                .build();
        int update = sysDepartmentDao.update(po);
        return update == 1;
    }

    @Override
    public boolean deleteDepartment(Long departmentId) {
        int delete = sysDepartmentDao.delete(departmentId);
        return delete == 1;
    }

    @Override
    public SystemDepartmentVO queryDepartmentByDepartmentId(Long departmentId) {
        SysDepartmentPO po = sysDepartmentDao.queryById(departmentId);
        if (po == null) {
            return null;
        }
        return SystemDepartmentVO.builder()
                .id(po.getId())
                .orgId(po.getOrgId())
                .parentId(po.getParentId())
                .deptName(po.getDeptName())
                .deptCode(po.getDeptCode())
                .leaderUserId(po.getLeaderUserId())
                .sortOrder(po.getSortOrder())
                .status(po.getStatus())
                .remark(po.getRemark())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build();
    }
}
