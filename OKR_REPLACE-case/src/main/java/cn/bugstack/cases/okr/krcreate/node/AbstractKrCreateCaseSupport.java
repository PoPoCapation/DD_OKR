package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractKrCreateCaseSupport extends AbstractMultiThreadStrategyRouter<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> {

    @Resource
    protected IOkrKeyResultRepository keyResultRepository;

    @Override
    protected void multiThread(OkrKeyResultVO requestParameter, KrCreateCaseFactory.KrCreateContext dynamicContext) throws ExecutionException, TimeoutException {
    }
}
