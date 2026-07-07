package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrTaskVO;

import java.util.List;

/**
 * Task（任务）Service
 */
public interface IOkrTaskService {

    /** 创建任务（校验 KR 所属目标在可编辑范围内；currentUserId 用于权限与审计） */
    void createTask(Long currentUserId, OkrTaskVO vo);

    /** 更新任务（状态变更写 TASK 进度流水 + 操作日志；currentUserId 用于审计） */
    void updateTask(Long currentUserId, OkrTaskVO vo);

    /** 删除任务（逻辑删除；currentUserId 用于审计） */
    void deleteTask(Long currentUserId, Long taskId);

    /** 根据KR ID查询其下所有任务（校验当前用户对该 KR 所属 O 的可见性） */
    List<OkrTaskVO> queryTaskListByKrId(Long currentUserId, Long krId);

    /** 指派任务给用户（全删全插；currentUserId 用于审计） */
    void assignUsers(Long currentUserId, Long taskId, List<Long> userIds);

    /** 查询我的任务（当前用户关联的任务） */
    List<OkrTaskVO> myTasks(Long currentUserId);

    /** 查询当前用户所在部门的任务 */
    List<OkrTaskVO> queryDepartmentTasks(Long currentUserId);

    /** 查询所有任务（管理视图） */
    List<OkrTaskVO> queryAllTasks();
}
