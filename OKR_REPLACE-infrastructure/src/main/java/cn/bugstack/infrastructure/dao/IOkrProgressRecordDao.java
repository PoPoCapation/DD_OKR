package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrProgressRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IOkrProgressRecordDao {
    int insert(OkrProgressRecordPO po);
    OkrProgressRecordPO queryById(Long id);
    int update(OkrProgressRecordPO po);
    int delete(Long id);

    /** 按目标查询进度记录 */
    List<OkrProgressRecordPO> queryByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /** 按操作人查询 */
    List<OkrProgressRecordPO> queryByOperator(Long operatorId);
}
