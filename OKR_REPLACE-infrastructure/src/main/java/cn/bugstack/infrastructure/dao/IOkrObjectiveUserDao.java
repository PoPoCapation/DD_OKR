package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrObjectiveUserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与O关联表 DAO
 */
@Mapper
public interface IOkrObjectiveUserDao {

    /** 新增用户与O关联 */
    int insert(OkrObjectiveUserPO po);

    /** 根据ID查询用户与O关联 */
    OkrObjectiveUserPO queryById(Long id);

    /** 根据ID更新用户与O关联 */
    int update(OkrObjectiveUserPO po);

    /** 根据ID物理删除用户与O关联 */
    int delete(Long id);
}
