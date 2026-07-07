package cn.bugstack.cases.okr.taskquery.node;

import cn.bugstack.cases.okr.taskquery.factory.TaskQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 节点3（链尾）：返回结果 */
@Slf4j
@Service("TaskQueryEndNode")
public class TaskQueryEndNode extends AbstractTaskQueryCaseSupport {
    @Override
    protected List<OkrTaskVO> doApply(Long currentUserId, TaskQueryCaseFactory.TaskQueryContext ctx) throws Exception {
        return ctx.getTasks();
    }

    @Override
    public StrategyHandler<Long, TaskQueryCaseFactory.TaskQueryContext, List<OkrTaskVO>> get(Long currentUserId, TaskQueryCaseFactory.TaskQueryContext ctx) throws Exception {
        return null;
    }
}
