package cn.bugstack.cases.okr.query.node;

import cn.bugstack.cases.okr.query.factory.QueryObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点1：算可见用户ID（自己 + 上级 + 下级递归）
 */
@Slf4j
@Service("LoadVisibleUsersNode")
public class LoadVisibleUsersNode extends AbstractQueryObjectiveCaseSupport {

    @Resource(name = "QueryObjectiveNode")
    private QueryObjectiveNode queryObjectiveNode;

    @Override
    protected List<OkrObjectiveVO> doApply(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        context.setVisibleUserIds(userService.queryVisibleUserIds(currentUserId));
        return router(currentUserId, context);
    }

    @Override
    public StrategyHandler<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> get(Long currentUserId, QueryObjectiveCaseFactory.QueryContext context) throws Exception {
        return queryObjectiveNode;
    }
}
