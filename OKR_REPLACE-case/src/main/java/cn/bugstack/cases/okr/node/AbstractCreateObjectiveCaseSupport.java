package cn.bugstack.cases.okr.node;

import cn.bugstack.cases.okr.factory.CreateObjectiveCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 创建目标节点基类
 */
public abstract class AbstractCreateObjectiveCaseSupport extends AbstractMultiThreadStrategyRouter<Long, CreateObjectiveCaseFactory.ObjectiveContext, Boolean> {

    @Resource
    protected IOkrObjectiveRepository objectiveRepository;
    @Resource
    protected IUserService userService;

    @Override
    protected void multiThread(Long requestParameter, CreateObjectiveCaseFactory.ObjectiveContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
    }
}
