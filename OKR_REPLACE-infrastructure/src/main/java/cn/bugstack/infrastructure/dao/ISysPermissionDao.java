package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysPermissionPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限表 DAO
 */
@Mapper
public interface ISysPermissionDao {

    /** 新增权限 */
    int insert(SysPermissionPO po);

    /** 根据ID查询权限（不含已删除） */
    SysPermissionPO queryById(Long id);

    /** 根据ID更新权限 */
    int update(SysPermissionPO po);

    /** 根据ID逻辑删除权限 */
    int delete(Long id);
}
