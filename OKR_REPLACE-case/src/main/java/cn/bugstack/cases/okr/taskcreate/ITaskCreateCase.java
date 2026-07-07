package cn.bugstack.cases.okr.taskcreate;

import cn.bugstack.domain.activity.model.entity.OkrTaskVO;

/**
 * 创建 Task 用例编排
 */
public interface ITaskCreateCase {
    Boolean createTask(OkrTaskVO vo);
}
