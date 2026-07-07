package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/** 节点1：设默认值 */
@Slf4j
@Service("SetDefaultKrNode")
public class SetDefaultKrNode extends AbstractKrCreateCaseSupport {
    @Resource(name = "CreateKrNode")
    private CreateKrNode createKrNode;

    @Override
    protected Boolean doApply(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        if (vo.getStatus() == null) vo.setStatus("todo");
        if (vo.getCompletionRate() == null) vo.setCompletionRate(BigDecimal.ZERO);
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> get(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return createKrNode;
    }
}
