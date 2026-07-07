package cn.bugstack.cases.okr.krquery.node;

import cn.bugstack.cases.okr.krquery.factory.KrQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("KrQueryRootNode")
public class KrQueryRootNode extends AbstractKrQueryCaseSupport {
    @Resource(name = "CheckVisibleKrNode")
    private CheckVisibleKrNode checkVisibleKrNode;

    @Override
    protected List<OkrKeyResultVO> doApply(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        return router(currentUserId, ctx);
    }

    @Override
    public StrategyHandler<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> get(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        return checkVisibleKrNode;
    }
}
