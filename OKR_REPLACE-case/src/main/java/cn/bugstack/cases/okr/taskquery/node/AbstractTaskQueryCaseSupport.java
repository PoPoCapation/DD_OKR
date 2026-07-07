package cn.bugstack.cases.okr.taskquery.node;

import cn.bugstack.cases.okr.taskquery.factory.TaskQueryCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskRepository;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractTaskQueryCaseSupport extends AbstractMultiThreadStrategyRouter<Long, TaskQueryCaseFactory.TaskQueryContext, List<OkrTaskVO>> {

    @Resource
    protected IUserService userService;
    @Resource
    protected IOkrKeyResultRepository keyResultRepository;
    @Resource
    protected IOkrObjectiveRepository objectiveRepository;
    @Resource
    protected IOkrTaskRepository taskRepository;

    @Override
    protected void multiThread(Long requestParameter, TaskQueryCaseFactory.TaskQueryContext dynamicContext) throws ExecutionException, TimeoutException {
    }
}
