package cn.bugstack.cases.auth.register.factory;

import cn.bugstack.cases.auth.register.node.RegisterRootNode;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterCaseFactory {

    @Resource(name = "RegisterRootNode")
    private RegisterRootNode rootNode;

    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterContext {
        private String password;
        private String username;
        private SystemUserVO user;
        private List<String> roleCodes;
        private List<String> permissionCodes;
        private String token;
    }
}
