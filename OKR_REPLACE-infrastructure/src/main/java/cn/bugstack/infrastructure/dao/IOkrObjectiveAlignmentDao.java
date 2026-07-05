package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrObjectiveAlignmentPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OKR目标对齐关系表 DAO
 */
@Mapper
public interface IOkrObjectiveAlignmentDao {

    /** 新增对齐关系 */
    int insert(OkrObjectiveAlignmentPO po);

    /** 根据ID查询对齐关系（不含已删除） */
    OkrObjectiveAlignmentPO queryById(Long id);

    /** 根据ID更新对齐关系 */
    int update(OkrObjectiveAlignmentPO po);

    /** 根据ID逻辑删除对齐关系 */
    int delete(Long id);
}
