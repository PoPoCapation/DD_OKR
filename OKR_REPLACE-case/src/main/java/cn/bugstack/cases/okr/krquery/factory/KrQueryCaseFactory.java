package cn.bugstack.cases.okr.krquery.factory;

import cn.bugstack.cases.okr.krquery.node.KrQueryRootNode;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KrQueryCaseFactory {
    @Resource(name = "KrQueryRootNode")
    private KrQueryRootNode rootNode;

    public StrategyHandler<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KrQueryContext {
        private Long objectiveId;
        private List<Long> visibleUserIds;
        private List<OkrKeyResultVO> keyResults;
    }
}
