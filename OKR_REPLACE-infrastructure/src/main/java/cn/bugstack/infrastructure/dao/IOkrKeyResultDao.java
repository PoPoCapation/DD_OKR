package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrKeyResultPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * KR表 DAO
 */
@Mapper
public interface IOkrKeyResultDao {

    /** 新增KR */
    int insert(OkrKeyResultPO po);

    /** 根据ID查询KR（不含已删除） */
    OkrKeyResultPO queryById(Long id);

    /** 根据ID更新KR */
    int update(OkrKeyResultPO po);

    /** 根据ID逻辑删除KR */
    int delete(Long id);

    /** 根据目标ID查询其下所有 KR（不含已删除，按排序） */
    List<OkrKeyResultPO> queryByObjectiveId(Long objectiveId);
}
