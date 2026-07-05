package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrObjectivePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * O表 DAO
 */
@Mapper
public interface IOkrObjectiveDao {

    /** 新增Objective */
    int insert(OkrObjectivePO po);

    /** 根据ID查询Objective（不含已删除） */
    OkrObjectivePO queryById(Long id);

    /** 根据ID更新Objective */
    int update(OkrObjectivePO po);

    /** 根据ID逻辑删除Objective */
    int delete(Long id);
}
