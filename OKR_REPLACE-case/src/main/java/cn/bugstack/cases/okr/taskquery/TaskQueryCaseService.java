package cn.bugstack.cases.okr.taskquery;

import cn.bugstack.cases.okr.taskquery.factory.TaskQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaskQueryCaseService implements ITaskQueryCase {

    @Resource
    private TaskQueryCaseFactory factory;

    @Override
    public List<OkrTaskVO> queryTaskList(Long currentUserId, Long krId) {
        TaskQueryCaseFactory.TaskQueryContext ctx = new TaskQueryCaseFactory.TaskQueryContext();
        ctx.setKrId(krId);
        StrategyHandler<Long, TaskQueryCaseFactory.TaskQueryContext, List<OkrTaskVO>> handler = factory.strategyHandler();
        try {
            return handler.apply(currentUserId, ctx);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
