package cn.bugstack.cases.okr.node;

import cn.bugstack.cases.okr.factory.QueryObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点2：按可见用户查目标列表
 */
@Slf4j
@Service("QueryObjectiveNode")
public class QueryObjectiveNode extends AbstractQueryObjectiveCaseSupport {

    @Resource(name = "QueryObjectiveEndNode")
    private QueryObjectiveEndNode endNode;

    @Override
    protected List<OkrObjectiveVO> doApply(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        context.setObjectives(objectiveRepository.queryObjectiveListByUserIds(context.getVisibleUserIds()));
        return router(currentUserId, context);
    }

    @Override
    public StrategyHandler<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> get(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        return endNode;
    }
}
