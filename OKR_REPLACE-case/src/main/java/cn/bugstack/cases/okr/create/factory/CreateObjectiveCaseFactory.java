package cn.bugstack.cases.okr.create.factory;

import cn.bugstack.cases.okr.create.node.CreateObjectiveRootNode;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class CreateObjectiveCaseFactory {

    @Resource(name = "CreateObjectiveRootNode")
    private CreateObjectiveRootNode rootNode;

    public StrategyHandler<Long, CreateObjectiveCaseFactory.ObjectiveContext, Boolean> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ObjectiveContext {
        private OkrObjectiveVO objective;
    }
}
