package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.user.adapter.repository.IUserRepository;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.infrastructure.dao.ISysUserDao;
import cn.bugstack.infrastructure.dao.po.SysUserPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository implements IUserRepository {
    @Resource
    private ISysUserDao sysUserDao;

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
}
