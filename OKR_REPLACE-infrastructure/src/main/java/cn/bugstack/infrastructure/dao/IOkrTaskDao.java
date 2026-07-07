package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrTaskPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Task表 DAO
 */
@Mapper
public interface IOkrTaskDao {

    /** 新增Task */
    int insert(OkrTaskPO po);

    /** 根据ID查询Task（不含已删除） */
    OkrTaskPO queryById(Long id);

    /** 根据ID更新Task */
    int update(OkrTaskPO po);

    /** 根据ID逻辑删除Task */
    int delete(Long id);

    /** 根据KR ID查询其下所有任务（不含已删除） */
    List<OkrTaskPO> queryByKrId(Long krId);
}
