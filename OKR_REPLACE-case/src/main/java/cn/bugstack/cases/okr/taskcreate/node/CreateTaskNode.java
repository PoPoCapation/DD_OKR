package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 节点2：创建 Task */
@Slf4j
@Service("CreateTaskNode")
public class CreateTaskNode extends AbstractTaskCreateCaseSupport {
    @Resource(name = "TaskCreateEndNode")
    private TaskCreateEndNode endNode;

    @Override
    protected Boolean doApply(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        if (!taskRepository.createTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_CREATE_FAIL.getCode(), ResponseCode.OKR_TASK_CREATE_FAIL.getInfo());
        }
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> get(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return endNode;
    }
}
