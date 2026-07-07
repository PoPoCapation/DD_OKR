package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 节点2：持久化 KR（委托 domain service，由 service 完成默认值/持久化/重算/审计） */
@Slf4j
@Service("CreateKrNode")
public class CreateKrNode extends AbstractKrCreateCaseSupport {
    @Resource(name = "KrCreateEndNode")
    private KrCreateEndNode endNode;

    @Override
    protected Boolean doApply(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        // 调用 domain service：内部完成默认值、持久化、目标进度重算、进度流水与操作日志
        keyResultService.createKeyResult(ctx.getCurrentUserId(), vo);
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> get(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return endNode;
    }
}