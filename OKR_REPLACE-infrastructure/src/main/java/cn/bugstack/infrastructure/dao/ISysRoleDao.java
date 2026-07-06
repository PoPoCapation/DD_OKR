package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysRolePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色表 DAO
 */
@Mapper
public interface ISysRoleDao {

    /** 新增角色 */
    int insert(SysRolePO po);

    /** 根据ID查询角色（不含已删除） */
    SysRolePO queryById(Long id);

    /** 根据ID更新角色 */
    int update(SysRolePO po);

    /** 根据ID逻辑删除角色 */
    int delete(Long id);

    /** 根据用户ID查询其所有角色的数据范围列表（用于取最宽 data_scope） */
    List<String> queryDataScopesByUserId(Long userId);
}
