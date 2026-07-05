package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrProgressRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OKR进度记录表 DAO
 */
@Mapper
public interface IOkrProgressRecordDao {

    /** 新增进度记录 */
    int insert(OkrProgressRecordPO po);

    /** 根据ID查询进度记录（不含已删除） */
    OkrProgressRecordPO queryById(Long id);

    /** 根据ID更新进度记录 */
    int update(OkrProgressRecordPO po);

    /** 根据ID逻辑删除进度记录 */
    int delete(Long id);
}
