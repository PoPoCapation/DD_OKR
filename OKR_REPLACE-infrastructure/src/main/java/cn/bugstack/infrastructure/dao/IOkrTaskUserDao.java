package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrTaskUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /** 查询任务关联的用户ID列表 */
    List<Long> queryUserIdsByTaskId(Long taskId);

    /** 删除任务的所有关联 */
    int deleteByTaskId(Long taskId);

    /** 查询用户关联的任务ID列表 */
    List<Long> queryTaskIdsByUserId(Long userId);

    /** 批量新增关联 */
    int insertBatch(@Param("list") List<OkrTaskUserPO> list);
}
