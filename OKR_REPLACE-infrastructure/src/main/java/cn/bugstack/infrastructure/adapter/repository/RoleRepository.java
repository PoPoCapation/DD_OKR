package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.user.adapter.repository.IRoleRepository;
import cn.bugstack.domain.user.model.entity.SystemRoleVO;
import cn.bugstack.infrastructure.dao.ISysRoleDao;
import cn.bugstack.infrastructure.dao.ISysRolePermissionDao;
import cn.bugstack.infrastructure.dao.po.SysRolePO;
import cn.bugstack.infrastructure.dao.po.SysRolePermissionPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class RoleRepository implements IRoleRepository {
    @Resource
    private ISysRoleDao sysRoleDao;
    @Resource
    private ISysRolePermissionDao sysRolePermissionDao;

    @Override
    public boolean createRole(SystemRoleVO systemRoleVO) {
        SysRolePO sysRolePO = SysRolePO.builder()
                .orgId(systemRoleVO.getOrgId())
                .roleCode(systemRoleVO.getRoleCode())
                .roleName(systemRoleVO.getRoleName())
                .dataScope(systemRoleVO.getDataScope())
                .sortOrder(systemRoleVO.getSortOrder())
                .status(systemRoleVO.getStatus())
                .remark(systemRoleVO.getRemark())
                .isDeleted(systemRoleVO.getIsDeleted())
                .createtime(systemRoleVO.getCreatetime())
                .updatetime(systemRoleVO.getUpdatetime())
                .build();
        int insert = sysRoleDao.insert(sysRolePO);
        return insert == 1;
    }

    @Override
    public boolean updateRole(SystemRoleVO systemRoleVO) {
        SysRolePO sysRolePO = SysRolePO.builder()
                .id(systemRoleVO.getId())
                .orgId(systemRoleVO.getOrgId())
                .roleCode(systemRoleVO.getRoleCode())
                .roleName(systemRoleVO.getRoleName())
                .dataScope(systemRoleVO.getDataScope())
                .sortOrder(systemRoleVO.getSortOrder())
                .status(systemRoleVO.getStatus())
                .remark(systemRoleVO.getRemark())
                .isDeleted(systemRoleVO.getIsDeleted())
                .createtime(systemRoleVO.getCreatetime())
                .updatetime(systemRoleVO.getUpdatetime())
                .build();
        int update = sysRoleDao.update(sysRolePO);
        return update == 1;
    }

    @Override
    public boolean deleteRole(Long roleId) {
        int delete = sysRoleDao.delete(roleId);
        return delete == 1;
    }

    @Override
    public SystemRoleVO queryRoleByRoleId(Long roleId) {
        SysRolePO sysRolePO = sysRoleDao.queryById(roleId);
        if (sysRolePO == null) {
            return null;
        }
        return SystemRoleVO.builder()
                .id(sysRolePO.getId())
                .orgId(sysRolePO.getOrgId())
                .roleCode(sysRolePO.getRoleCode())
                .roleName(sysRolePO.getRoleName())
                .dataScope(sysRolePO.getDataScope())
                .sortOrder(sysRolePO.getSortOrder())
                .status(sysRolePO.getStatus())
                .remark(sysRolePO.getRemark())
                .isDeleted(sysRolePO.getIsDeleted())
                .createtime(sysRolePO.getCreatetime())
                .updatetime(sysRolePO.getUpdatetime())
                .build();
    }

    @Override
    public boolean setRolePermissions(Long roleId, List<Long> permissionIds) {
        Set<Long> current = new HashSet<>(safeList(sysRolePermissionDao.queryPermissionIdsByRoleId(roleId)));
        Set<Long> target = new HashSet<>(safeList(permissionIds));
        // 要移除的：当前有但目标没有
        List<Long> toRemove = current.stream().filter(id -> !target.contains(id)).collect(Collectors.toList());
        // 要新增的：目标有但当前没有
        List<Long> toAdd = target.stream().filter(id -> !current.contains(id)).collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            sysRolePermissionDao.deleteByRoleIdAndPermissionIds(roleId, toRemove);
        }
        if (!toAdd.isEmpty()) {
            sysRolePermissionDao.insertBatch(buildPOs(roleId, toAdd));
        }
        return true;
    }

    @Override
    public boolean addRolePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        Set<Long> current = new HashSet<>(safeList(sysRolePermissionDao.queryPermissionIdsByRoleId(roleId)));
        // 去重：仅插入当前不存在的权限
        List<Long> toAdd = permissionIds.stream()
                .filter(id -> !current.contains(id))
                .distinct()
                .collect(Collectors.toList());
        if (toAdd.isEmpty()) {
            return true;
        }
        sysRolePermissionDao.insertBatch(buildPOs(roleId, toAdd));
        return true;
    }

    @Override
    public boolean removeRolePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        sysRolePermissionDao.deleteByRoleIdAndPermissionIds(roleId, permissionIds);
        return true;
    }

    @Override
    public List<Long> queryPermissionIdsByRoleId(Long roleId) {
        return safeList(sysRolePermissionDao.queryPermissionIdsByRoleId(roleId));
    }

    /** 构建角色权限关联 PO 列表 */
    private List<SysRolePermissionPO> buildPOs(Long roleId, List<Long> permissionIds) {
        return permissionIds.stream()
                .map(pid -> SysRolePermissionPO.builder().roleId(roleId).permissionId(pid).build())
                .collect(Collectors.toList());
    }

    /** DAO 返回 null 时兜底为空集合，避免 NPE */
    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
