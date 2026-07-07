package cn.bugstack.cases.okr.taskquery.node;

import cn.bugstack.cases.okr.taskquery.factory.TaskQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 节点2：查询 Task 列表 */
@Slf4j
@Service("QueryTaskNode")
public class QueryTaskNode extends AbstractTaskQueryCaseSupport {
    @Resource(name = "TaskQueryEndNode")
    private TaskQueryEndNode endNode;

    @Override
    protected List<OkrTaskVO> doApply(Long currentUserId, TaskQueryCaseFactory.TaskQueryContext ctx) throws Exception {
        ctx.setTasks(taskRepository.queryTaskListByKrId(ctx.getKrId()));
        return router(currentUserId, ctx);
    }

    @Override
    public StrategyHandler<Long, TaskQueryCaseFactory.TaskQueryContext, List<OkrTaskVO>> get(Long currentUserId, TaskQueryCaseFactory.TaskQueryContext ctx) throws Exception {
        return endNode;
    }
}
