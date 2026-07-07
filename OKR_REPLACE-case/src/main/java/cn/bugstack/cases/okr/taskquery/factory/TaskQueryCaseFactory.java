package cn.bugstack.cases.okr.taskquery.factory;

import cn.bugstack.cases.okr.taskquery.node.TaskQueryRootNode;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskQueryCaseFactory {
    @Resource(name = "TaskQueryRootNode")
    private TaskQueryRootNode rootNode;

    public StrategyHandler<Long, TaskQueryCaseFactory.TaskQueryContext, List<OkrTaskVO>> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskQueryContext {
        private Long krId;
        private List<OkrTaskVO> tasks;
    }
}
