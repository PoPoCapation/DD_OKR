package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrCheckInPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOkrCheckInDao {
    int insert(OkrCheckInPO po);
    OkrCheckInPO queryById(Long id);
    int update(OkrCheckInPO po);
    int delete(Long id);
    List<OkrCheckInPO> queryByObjectiveId(Long objectiveId);
}
