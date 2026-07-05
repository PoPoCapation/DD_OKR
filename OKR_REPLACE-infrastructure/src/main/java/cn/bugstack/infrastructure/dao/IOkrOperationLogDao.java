package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrOperationLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OKR操作日志表 DAO
 */
@Mapper
public interface IOkrOperationLogDao {

    /** 新增操作日志 */
    int insert(OkrOperationLogPO po);

    /** 根据ID查询操作日志（不含已删除） */
    OkrOperationLogPO queryById(Long id);

    /** 根据ID更新操作日志 */
    int update(OkrOperationLogPO po);

    /** 根据ID逻辑删除操作日志 */
    int delete(Long id);
}
