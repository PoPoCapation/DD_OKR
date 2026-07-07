package cn.bugstack.domain.activity.adapter.repository;

import java.util.List;

/**
 * Task-User 关联 Repository
 */
public interface IOkrTaskUserRepository {

    /** 设置任务关联的用户（全删全插） */
    void assignUsers(Long taskId, List<Long> userIds);

    /** 查询任务关联的用户ID列表 */
    List<Long> queryUserIdsByTaskId(Long taskId);

    /** 查询用户关联的任务ID列表 */
    List<Long> queryTaskIdsByUserId(Long userId);
}
