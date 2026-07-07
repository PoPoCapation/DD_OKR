package cn.bugstack.cases.okr.krcreate.factory;

import cn.bugstack.cases.okr.krcreate.node.KrCreateRootNode;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class KrCreateCaseFactory {
    @Resource(name = "KrCreateRootNode")
    private KrCreateRootNode rootNode;

    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KrCreateContext {
        /** 操作人ID（权限校验 + 审计） */
        private Long currentUserId;
    }
}
