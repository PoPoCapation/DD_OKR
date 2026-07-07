package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/** 节点1：设默认值 */
@Slf4j
@Service("SetDefaultTaskNode")
public class SetDefaultTaskNode extends AbstractTaskCreateCaseSupport {
    @Resource(name = "CreateTaskNode")
    private CreateTaskNode createTaskNode;

    @Override
    protected Boolean doApply(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        if (vo.getStatus() == null) vo.setStatus("todo");
        if (vo.getPriority() == null) vo.setPriority(2);
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> get(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return createTaskNode;
    }
}
