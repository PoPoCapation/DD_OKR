package cn.bugstack.cases.okr.query.node;

import cn.bugstack.cases.okr.query.factory.QueryObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点3（链尾）：返回查询结果
 */
@Slf4j
@Service("QueryObjectiveEndNode")
public class QueryObjectiveEndNode extends AbstractQueryObjectiveCaseSupport {

    @Override
    protected List<OkrObjectiveVO> doApply(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        return context.getObjectives();
    }

    @Override
    public StrategyHandler<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> get(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        return null;
    }
}
