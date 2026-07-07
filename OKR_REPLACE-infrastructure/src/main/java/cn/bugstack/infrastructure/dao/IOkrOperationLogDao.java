package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrOperationLogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IOkrOperationLogDao {
    int insert(OkrOperationLogPO po);
    OkrOperationLogPO queryById(Long id);
    int update(OkrOperationLogPO po);
    int delete(Long id);

    List<OkrOperationLogPO> queryByResource(@Param("resourceType") String resourceType, @Param("resourceId") Long resourceId);
    List<OkrOperationLogPO> queryByOperator(Long operatorId);
}
