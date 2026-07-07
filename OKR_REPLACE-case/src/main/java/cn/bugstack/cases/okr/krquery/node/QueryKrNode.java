package cn.bugstack.cases.okr.krquery.node;

import cn.bugstack.cases.okr.krquery.factory.KrQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 节点2：查询 KR 列表 */
@Slf4j
@Service("QueryKrNode")
public class QueryKrNode extends AbstractKrQueryCaseSupport {
    @Resource(name = "KrQueryEndNode")
    private KrQueryEndNode endNode;

    @Override
    protected List<OkrKeyResultVO> doApply(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        ctx.setKeyResults(keyResultRepository.queryKeyResultListByObjectiveId(ctx.getObjectiveId()));
        return router(currentUserId, ctx);
    }

    @Override
    public StrategyHandler<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> get(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        return endNode;
    }
}
