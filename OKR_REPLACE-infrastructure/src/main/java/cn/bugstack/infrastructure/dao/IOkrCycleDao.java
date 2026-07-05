package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrCyclePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OKR周期表 DAO
 */
@Mapper
public interface IOkrCycleDao {

    /** 新增周期 */
    int insert(OkrCyclePO po);

    /** 根据ID查询周期（不含已删除） */
    OkrCyclePO queryById(Long id);

    /** 根据ID更新周期 */
    int update(OkrCyclePO po);

    /** 根据ID逻辑删除周期 */
    int delete(Long id);
}
