package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrTaskUserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与Task关联表 DAO
 */
@Mapper
public interface IOkrTaskUserDao {

    /** 新增用户与Task关联 */
    int insert(OkrTaskUserPO po);

    /** 根据ID查询用户与Task关联 */
    OkrTaskUserPO queryById(Long id);

    /** 根据ID更新用户与Task关联 */
    int update(OkrTaskUserPO po);

    /** 根据ID物理删除用户与Task关联 */
    int delete(Long id);
}
