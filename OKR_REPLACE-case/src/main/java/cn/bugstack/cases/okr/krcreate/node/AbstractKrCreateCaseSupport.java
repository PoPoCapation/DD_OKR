package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.service.IOkrKeyResultService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractKrCreateCaseSupport extends AbstractMultiThreadStrategyRouter<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> {

    @Resource
    protected IOkrKeyResultService keyResultService;
    @Resource
    protected IOkrObjectiveRepository objectiveRepository;
    @Resource
    protected IUserService userService;

    @Override
    protected void multiThread(OkrKeyResultVO requestParameter, KrCreateCaseFactory.KrCreateContext dynamicContext) throws ExecutionException, TimeoutException {
    }
}
