package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.user.adapter.repository.IPermissionRepository;
import cn.bugstack.domain.user.model.entity.SystemPermissionVO;
import cn.bugstack.infrastructure.dao.ISysPermissionDao;
import cn.bugstack.infrastructure.dao.po.SysPermissionPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionRepository implements IPermissionRepository {
    @Resource
    private ISysPermissionDao sysPermissionDao;

    @Override
    public boolean createPermission(SystemPermissionVO systemPermissionVO) {
        SysPermissionPO po = SysPermissionPO.builder()
                .parentId(systemPermissionVO.getParentId())
                .permCode(systemPermissionVO.getPermCode())
                .permName(systemPermissionVO.getPermName())
                .permType(systemPermissionVO.getPermType())
                .path(systemPermissionVO.getPath())
                .sortOrder(systemPermissionVO.getSortOrder())
                .status(systemPermissionVO.getStatus())
                .remark(systemPermissionVO.getRemark())
                .isDeleted(systemPermissionVO.getIsDeleted())
                .createtime(systemPermissionVO.getCreatetime())
                .updatetime(systemPermissionVO.getUpdatetime())
                .build();
        int insert = sysPermissionDao.insert(po);
        return insert == 1;
    }

    @Override
    public boolean updatePermission(SystemPermissionVO systemPermissionVO) {
        SysPermissionPO po = SysPermissionPO.builder()
                .id(systemPermissionVO.getId())
                .parentId(systemPermissionVO.getParentId())
                .permCode(systemPermissionVO.getPermCode())
                .permName(systemPermissionVO.getPermName())
                .permType(systemPermissionVO.getPermType())
                .path(systemPermissionVO.getPath())
                .sortOrder(systemPermissionVO.getSortOrder())
                .status(systemPermissionVO.getStatus())
                .remark(systemPermissionVO.getRemark())
                .isDeleted(systemPermissionVO.getIsDeleted())
                .createtime(systemPermissionVO.getCreatetime())
                .updatetime(systemPermissionVO.getUpdatetime())
                .build();
        int update = sysPermissionDao.update(po);
        return update == 1;
    }

    @Override
    public boolean deletePermission(Long permissionId) {
        int delete = sysPermissionDao.delete(permissionId);
        return delete == 1;
    }

    @Override
    public SystemPermissionVO queryPermissionByPermissionId(Long permissionId) {
        SysPermissionPO po = sysPermissionDao.queryById(permissionId);
        if (po == null) {
            return null;
        }
        return SystemPermissionVO.builder()
                .id(po.getId())
                .parentId(po.getParentId())
                .permCode(po.getPermCode())
                .permName(po.getPermName())
                .permType(po.getPermType())
                .path(po.getPath())
                .sortOrder(po.getSortOrder())
                .status(po.getStatus())
                .remark(po.getRemark())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build();
    }
}
