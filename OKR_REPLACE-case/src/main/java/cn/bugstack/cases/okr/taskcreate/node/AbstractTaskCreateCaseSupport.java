package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.domain.activity.service.IOkrTaskService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractTaskCreateCaseSupport extends AbstractMultiThreadStrategyRouter<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> {

    @Resource
    protected IOkrTaskService taskService;
    @Resource
    protected IOkrKeyResultRepository keyResultRepository;
    @Resource
    protected IOkrObjectiveRepository objectiveRepository;
    @Resource
    protected IUserService userService;

    @Override
    protected void multiThread(OkrTaskVO requestParameter, TaskCreateCaseFactory.TaskCreateContext dynamicContext) throws ExecutionException, TimeoutException {
    }
}
