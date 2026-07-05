package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 DAO
 */
@Mapper
public interface ISysRoleDao {

    /** 新增角色 */
    int insert(SysRoleEntity po);

    /** 根据ID查询角色（不含已删除） */
    SysRoleEntity queryById(Long id);

    /** 根据ID更新角色 */
    int update(SysRoleEntity po);

    /** 根据ID逻辑删除角色 */
    int delete(Long id);
}
