package cn.bugstack.cases.okr.taskcreate.factory;

import cn.bugstack.cases.okr.taskcreate.node.TaskCreateRootNode;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class TaskCreateCaseFactory {
    @Resource(name = "TaskCreateRootNode")
    private TaskCreateRootNode rootNode;

    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class TaskCreateContext {
    }
}
