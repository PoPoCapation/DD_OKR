package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysDepartmentPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门表 DAO
 */
@Mapper
public interface ISysDepartmentDao {

    /** 新增部门 */
    int insert(SysDepartmentPO po);

    /** 根据ID查询部门（不含已删除） */
    SysDepartmentPO queryById(Long id);

    /** 根据ID更新部门 */
    int update(SysDepartmentPO po);

    /** 根据ID逻辑删除部门 */
    int delete(Long id);
}
