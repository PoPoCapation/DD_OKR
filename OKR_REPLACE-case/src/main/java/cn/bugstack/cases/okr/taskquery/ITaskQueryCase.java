package cn.bugstack.cases.okr.taskquery;

import cn.bugstack.domain.activity.model.entity.OkrTaskVO;

import java.util.List;

/**
 * 查询 Task 用例编排（校验 krId→KR→O 可见 + 查 Task）
 */
public interface ITaskQueryCase {
    List<OkrTaskVO> queryTaskList(Long currentUserId, Long krId);
}
