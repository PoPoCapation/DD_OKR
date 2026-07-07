package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("TaskCreateRootNode")
public class TaskCreateRootNode extends AbstractTaskCreateCaseSupport {
    @Resource(name = "ValidateTaskNode")
    private ValidateTaskNode validateTaskNode;

    @Override
    protected Boolean doApply(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> get(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return validateTaskNode;
    }
}
