package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrCheckInPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OKR Check-in主表 DAO
 */
@Mapper
public interface IOkrCheckInDao {

    /** 新增Check-in */
    int insert(OkrCheckInPO po);

    /** 根据ID查询Check-in（不含已删除） */
    OkrCheckInPO queryById(Long id);

    /** 根据ID更新Check-in */
    int update(OkrCheckInPO po);

    /** 根据ID逻辑删除Check-in */
    int delete(Long id);
}
