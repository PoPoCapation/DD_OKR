package cn.bugstack.cases.okr.krquery.node;

import cn.bugstack.cases.okr.krquery.factory.KrQueryCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractKrQueryCaseSupport extends AbstractMultiThreadStrategyRouter<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> {

    @Resource
    protected IUserService userService;
    @Resource
    protected IOkrObjectiveRepository objectiveRepository;
    @Resource
    protected IOkrKeyResultRepository keyResultRepository;

    @Override
    protected void multiThread(Long requestParameter, KrQueryCaseFactory.KrQueryContext dynamicContext) throws ExecutionException, TimeoutException {
    }
}
