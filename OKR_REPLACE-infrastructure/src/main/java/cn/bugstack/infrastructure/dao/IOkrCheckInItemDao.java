package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrCheckInItemPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OKR Check-in明细表 DAO
 */
@Mapper
public interface IOkrCheckInItemDao {

    /** 新增Check-in明细 */
    int insert(OkrCheckInItemPO po);

    /** 根据ID查询Check-in明细（不含已删除） */
    OkrCheckInItemPO queryById(Long id);

    /** 根据ID更新Check-in明细 */
    int update(OkrCheckInItemPO po);

    /** 根据ID逻辑删除Check-in明细 */
    int delete(Long id);
}
