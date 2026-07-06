package cn.bugstack.cases.auth.factory;

import cn.bugstack.cases.auth.node.LoginRootNode;
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

/**
 * 登录用例工厂：持有责任链根节点 + 编排上下文
 */
@Service
public class LoginCaseFactory {

    @Resource(name = "LoginRootNode")
    private LoginRootNode rootNode;

    public StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> strategyHandler() {
        return rootNode;
    }

    /** 登录编排上下文（节点间共享） */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginContext {
        /** 入参：明文密码 */
        private String password;
        /** 中间：查到的用户 */
        private SystemUserVO user;
        /** 中间：角色编码列表 */
        private List<String> roleCodes;
        /** 中间：权限编码列表 */
        private List<String> permissionCodes;
        /** 中间：签发的 token */
        private String token;
    }
}
