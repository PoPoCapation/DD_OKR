package cn.bugstack.domain.activity.adapter.repository;

import cn.bugstack.domain.activity.model.entity.OkrTaskVO;

import java.util.List;

/**
 * Task Repository
 */
public interface IOkrTaskRepository {

    boolean createTask(OkrTaskVO vo);

    boolean updateTask(OkrTaskVO vo);

    boolean deleteTask(Long taskId);

    List<OkrTaskVO> queryTaskListByKrId(Long krId);

    /** 按任务ID列表批量查询任务 */
    List<OkrTaskVO> queryTasksByTaskIds(List<Long> taskIds);
}
