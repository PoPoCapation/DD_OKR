package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrTaskVO;

import java.util.List;

/**
 * Task（任务）Service
 */
public interface IOkrTaskService {

    /** 创建任务 */
    void createTask(OkrTaskVO vo);

    /** 更新任务 */
    void updateTask(OkrTaskVO vo);

    /** 删除任务（逻辑删除） */
    void deleteTask(Long taskId);

    /** 根据KR ID查询其下所有任务（校验当前用户对该 KR 所属 O 的可见性） */
    List<OkrTaskVO> queryTaskListByKrId(Long currentUserId, Long krId);
}
