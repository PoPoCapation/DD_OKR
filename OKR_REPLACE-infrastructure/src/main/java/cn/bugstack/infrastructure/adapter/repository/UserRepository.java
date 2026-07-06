package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.user.adapter.repository.IUserRepository;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.infrastructure.dao.ISysUserDao;
import cn.bugstack.infrastructure.dao.ISysDepartmentDao;
import cn.bugstack.infrastructure.dao.ISysRoleDao;
import cn.bugstack.infrastructure.dao.ISysUserRoleDao;
import cn.bugstack.infrastructure.dao.po.SysUserPO;
import cn.bugstack.infrastructure.dao.po.SysUserRolePO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepository implements IUserRepository {
    @Resource
    private ISysUserDao sysUserDao;
    @Resource
    private ISysUserRoleDao sysUserRoleDao;
    @Resource
    private ISysRoleDao sysRoleDao;
    @Resource
    private ISysDepartmentDao sysDepartmentDao;

    @Override
    public boolean createUser(SystemUserVO systemUserVO) {
        SysUserPO sysUserPO = SysUserPO.builder()
                .username(systemUserVO.getUsername())
                .account(systemUserVO.getAccount())
                .password(systemUserVO.getPassword())
                .departmentId(systemUserVO.getDepartmentId())
                .status(systemUserVO.getStatus())
                .isDeleted(systemUserVO.getIsDeleted())
                .createtime(systemUserVO.getCreatetime())
                .updatetime(systemUserVO.getUpdatetime())
                .build();
        int insert = sysUserDao.insert(sysUserPO);

        return insert == 1;

    }

    @Override
    public boolean updateUser(SystemUserVO systemUserVO) {
        SysUserPO sysUserPO = SysUserPO.builder()
                .id(systemUserVO.getId())
                .username(systemUserVO.getUsername())
                .account(systemUserVO.getAccount())
                .password(systemUserVO.getPassword())
                .departmentId(systemUserVO.getDepartmentId())
                .status(systemUserVO.getStatus())
                .isDeleted(systemUserVO.getIsDeleted())
                .createtime(systemUserVO.getCreatetime())
                .updatetime(systemUserVO.getUpdatetime())
                .build();
        int update = sysUserDao.update(sysUserPO);

        return update == 1;
    }

    @Override
    public boolean deleteUser(Long userId) {
        int delete = sysUserDao.delete(userId);
        return delete == 1;
    }

    @Override
    public SystemUserVO queryUserByUserId(Long userId) {
        SysUserPO sysUserPO = sysUserDao.queryById(userId);
        if (sysUserPO == null) {
            return null;
        }

        return SystemUserVO.builder()
                .id(sysUserPO.getId())
                .username(sysUserPO.getUsername())
                .account(sysUserPO.getAccount())
                .password(sysUserPO.getPassword())
                .departmentId(sysUserPO.getDepartmentId())
                .status(sysUserPO.getStatus())
                .isDeleted(sysUserPO.getIsDeleted())
                .createtime(sysUserPO.getCreatetime())
                .updatetime(sysUserPO.getUpdatetime())
                .build();
    }

    @Override
    public boolean setUserRoles(Long userId, List<Long> roleIds) {
        Set<Long> current = new HashSet<>(safeList(sysUserRoleDao.queryRoleIdsByUserId(userId)));
        Set<Long> target = new HashSet<>(safeList(roleIds));

        // 要移除的：当前有但目标没有
        List<Long> toRemove = current.stream().filter(id -> !target.contains(id)).collect(Collectors.toList());
        // 要新增的：目标有但当前没有
        List<Long> toAdd = target.stream().filter(id -> !current.contains(id)).collect(Collectors.toList());

        if (!toRemove.isEmpty()) {
            sysUserRoleDao.deleteByUserIdAndRoleIds(userId, toRemove);
        }
        if (!toAdd.isEmpty()) {
            sysUserRoleDao.insertBatch(buildPOs(userId, toAdd));
        }
        return true;
    }

    @Override
    public boolean addUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        Set<Long> current = new HashSet<>(safeList(sysUserRoleDao.queryRoleIdsByUserId(userId)));
        // 去重：仅插入当前不存在的角色
        List<Long> toAdd = roleIds.stream()
                .filter(id -> !current.contains(id))
                .distinct()
                .collect(Collectors.toList());
        if (toAdd.isEmpty()) {
            return true;
        }
        sysUserRoleDao.insertBatch(buildPOs(userId, toAdd));
        return true;
    }

    @Override
    public boolean removeUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        sysUserRoleDao.deleteByUserIdAndRoleIds(userId, roleIds);
        return true;
    }

    @Override
    public List<Long> queryRoleIdsByUserId(Long userId) {
        return safeList(sysUserRoleDao.queryRoleIdsByUserId(userId));
    }

    @Override
    public List<String> queryRoleCodesByUserId(Long userId) {
        return safeList(sysUserRoleDao.queryRoleCodesByUserId(userId));
    }

    @Override
    public List<String> queryPermissionCodesByUserId(Long userId) {
        return safeList(sysUserRoleDao.queryPermissionCodesByUserId(userId));
    }

    @Override
    public SystemUserVO queryUserByAccount(String account) {
        SysUserPO sysUserPO = sysUserDao.queryByAccount(account);
        if (sysUserPO == null) {
            return null;
        }
        return SystemUserVO.builder()
                .id(sysUserPO.getId())
                .username(sysUserPO.getUsername())
                .account(sysUserPO.getAccount())
                .password(sysUserPO.getPassword())
                .departmentId(sysUserPO.getDepartmentId())
                .status(sysUserPO.getStatus())
                .isDeleted(sysUserPO.getIsDeleted())
                .createtime(sysUserPO.getCreatetime())
                .updatetime(sysUserPO.getUpdatetime())
                .build();
    }

    /** 构建用户角色关联 PO 列表 */
    private List<SysUserRolePO> buildPOs(Long userId, List<Long> roleIds) {
        return roleIds.stream()
                .map(roleId -> SysUserRolePO.builder().userId(userId).roleId(roleId).build())
                .collect(Collectors.toList());
    }

    /** DAO 返回 null 时兜底为空集合，避免 NPE */
    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public String queryUserDataScope(Long userId) {
        List<String> scopes = sysRoleDao.queryDataScopesByUserId(userId);
        if (scopes == null || scopes.isEmpty()) {
            return "self";
        }
        // 取最宽：all > dept_and_below > dept > self
        if (scopes.contains("all")) return "all";
        if (scopes.contains("dept_and_below")) return "dept_and_below";
        if (scopes.contains("dept")) return "dept";
        return "self";
    }

    @Override
    public List<Long> queryDescendantDeptIds(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        return sysDepartmentDao.queryDescendantDeptIds(deptId);
    }

    @Override
    public List<Long> queryVisibleUserIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return sysUserDao.queryVisibleUserIds(userId);
    }
}