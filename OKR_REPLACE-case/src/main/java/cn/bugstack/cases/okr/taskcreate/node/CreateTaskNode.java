package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 节点2：持久化 Task（委托 domain service，由 service 完成默认值/权限已校验/持久化/审计） */
@Slf4j
@Service("CreateTaskNode")
public class CreateTaskNode extends AbstractTaskCreateCaseSupport {
    @Resource(name = "TaskCreateEndNode")
    private TaskCreateEndNode endNode;

    @Override
    protected Boolean doApply(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        // 调用 domain service：内部完成默认值、持久化、操作日志
        taskService.createTask(ctx.getCurrentUserId(), vo);
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> get(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return endNode;
    }
}
