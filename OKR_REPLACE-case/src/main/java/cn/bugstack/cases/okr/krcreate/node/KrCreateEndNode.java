package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 节点3（链尾）：返回成功 */
@Slf4j
@Service("KrCreateEndNode")
public class KrCreateEndNode extends AbstractKrCreateCaseSupport {
    @Override
    protected Boolean doApply(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return true;
    }

    @Override
    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> get(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return null;
    }
}
