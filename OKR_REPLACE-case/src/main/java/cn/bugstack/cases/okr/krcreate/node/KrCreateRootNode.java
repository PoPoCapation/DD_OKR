package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("KrCreateRootNode")
public class KrCreateRootNode extends AbstractKrCreateCaseSupport {
    @Resource(name = "SetDefaultKrNode")
    private SetDefaultKrNode setDefaultKrNode;

    @Override
    protected Boolean doApply(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> get(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return setDefaultKrNode;
    }
}
