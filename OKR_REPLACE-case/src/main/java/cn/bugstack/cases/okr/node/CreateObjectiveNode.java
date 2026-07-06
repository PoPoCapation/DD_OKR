package cn.bugstack.cases.okr.node;

import cn.bugstack.cases.okr.factory.CreateObjectiveCaseFactory;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点2：持久化目标（失败抛异常）
 */
@Slf4j
@Service("CreateObjectiveNode")
public class CreateObjectiveNode extends AbstractCreateObjectiveCaseSupport {

    @Resource(name = "CreateObjectiveEndNode")
    private CreateObjectiveEndNode endNode;

    @Override
    protected Boolean doApply(Long currentUserId, CreateObjectiveCaseFactory.ObjectiveContext context) throws Exception {
        if (!objectiveRepository.createObjective(context.getObjective())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_CREATE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_CREATE_FAIL.getInfo());
        }
        return router(currentUserId, context);
    }

    @Override
    public StrategyHandler<Long, CreateObjectiveCaseFactory.ObjectiveContext, Boolean> get(Long currentUserId, CreateObjectiveCaseFactory.ObjectiveContext context) throws Exception {
        return endNode;
    }
}
