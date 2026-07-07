package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrCheckInItemPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOkrCheckInItemDao {
    int insert(OkrCheckInItemPO po);
    OkrCheckInItemPO queryById(Long id);
    int update(OkrCheckInItemPO po);
    int delete(Long id);
    List<OkrCheckInItemPO> queryByCheckInId(Long checkInId);
}
