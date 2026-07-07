package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrTaskPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Task表 DAO
 */
@Mapper
public interface IOkrTaskDao {

    int insert(OkrTaskPO po);
    OkrTaskPO queryById(Long id);
    int update(OkrTaskPO po);
    int delete(Long id);
    List<OkrTaskPO> queryByKrId(Long krId);

    /** 按任务ID列表批量查询 */
    List<OkrTaskPO> queryByIds(@Param("ids") List<Long> ids);
}
