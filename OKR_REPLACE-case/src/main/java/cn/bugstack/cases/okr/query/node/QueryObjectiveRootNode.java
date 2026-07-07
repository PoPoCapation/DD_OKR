package cn.bugstack.cases.okr.query.node;

import cn.bugstack.cases.okr.query.factory.QueryObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("QueryObjectiveRootNode")
public class QueryObjectiveRootNode extends AbstractQueryObjectiveCaseSupport {

    @Resource(name = "LoadVisibleUsersNode")
    private LoadVisibleUsersNode loadVisibleUsersNode;

    @Override
    protected List<OkrObjectiveVO> doApply(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        return router(currentUserId, context);
    }

    @Override
    public StrategyHandler<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> get(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        return loadVisibleUsersNode;
    }
}
