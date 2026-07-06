package cn.bugstack.cases.okr.node;

import cn.bugstack.cases.okr.factory.CreateObjectiveCaseFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点3（链尾）：返回成功
 */
@Slf4j
@Service("CreateObjectiveEndNode")
public class CreateObjectiveEndNode extends AbstractCreateObjectiveCaseSupport {

    @Override
    protected Boolean doApply(Long currentUserId, CreateObjectiveCaseFactory.ObjectiveContext context) throws Exception {
        return true;
    }

    @Override
    public StrategyHandler<Long, CreateObjectiveCaseFactory.ObjectiveContext, Boolean> get(Long currentUserId, CreateObjectiveCaseFactory.ObjectiveContext context) throws Exception {
        return null;
    }
}
