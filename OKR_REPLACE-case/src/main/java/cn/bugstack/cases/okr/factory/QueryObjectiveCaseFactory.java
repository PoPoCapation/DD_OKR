package cn.bugstack.cases.okr.factory;

import cn.bugstack.cases.okr.node.QueryObjectiveRootNode;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryObjectiveCaseFactory {

    @Resource(name = "QueryObjectiveRootNode")
    private QueryObjectiveRootNode rootNode;

    public StrategyHandler<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryContext {
        /** 可见用户ID列表（自己+上级+下级递归） */
        private List<Long> visibleUserIds;
        /** 查询结果 */
        private List<OkrObjectiveVO> objectives;
    }
}
