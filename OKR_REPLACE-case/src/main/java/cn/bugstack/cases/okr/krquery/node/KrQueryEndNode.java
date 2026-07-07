package cn.bugstack.cases.okr.krquery.node;

import cn.bugstack.cases.okr.krquery.factory.KrQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 节点3（链尾）：返回结果 */
@Slf4j
@Service("KrQueryEndNode")
public class KrQueryEndNode extends AbstractKrQueryCaseSupport {
    @Override
    protected List<OkrKeyResultVO> doApply(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        return ctx.getKeyResults();
    }

    @Override
    public StrategyHandler<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> get(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        return null;
    }
}
