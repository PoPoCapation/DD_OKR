package cn.bugstack.cases.okr.node;

import cn.bugstack.cases.okr.factory.QueryObjectiveCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 查询目标节点基类
 */
public abstract class AbstractQueryObjectiveCaseSupport extends AbstractMultiThreadStrategyRouter<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> {

    @Resource
    protected IUserService userService;
    @Resource
    protected IOkrObjectiveRepository objectiveRepository;

    @Override
    protected void multiThread(Long requestParameter, QueryObjectiveCaseFactory.QueryContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
    }
}
