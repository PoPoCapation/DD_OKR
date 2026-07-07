package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskRepository;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractTaskCreateCaseSupport extends AbstractMultiThreadStrategyRouter<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> {

    @Resource
    protected IOkrTaskRepository taskRepository;

    @Override
    protected void multiThread(OkrTaskVO requestParameter, TaskCreateCaseFactory.TaskCreateContext dynamicContext) throws ExecutionException, TimeoutException {
    }
}
