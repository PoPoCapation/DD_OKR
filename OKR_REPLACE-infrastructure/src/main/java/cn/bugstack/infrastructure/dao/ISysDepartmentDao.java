package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysDepartmentPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    /** 查询本部门及所有下级部门ID（CTE 递归，含自身） */
    List<Long> queryDescendantDeptIds(Long deptId);
}
