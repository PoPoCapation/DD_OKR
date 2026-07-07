package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 节点3（链尾）：返回成功 */
@Slf4j
@Service("TaskCreateEndNode")
public class TaskCreateEndNode extends AbstractTaskCreateCaseSupport {
    @Override
    protected Boolean doApply(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return true;
    }

    @Override
    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> get(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return null;
    }
}
