package cn.bugstack.cases.auth.login.node;

import cn.bugstack.cases.auth.login.factory.LoginCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录链头：直接路由到下一节点
 */
@Slf4j
@Service("LoginRootNode")
public class LoginRootNode extends AbstractLoginCaseSupport {

    @Resource(name = "LoginVerifyPasswordNode")
    private VerifyPasswordNode verifyPasswordNode;

    @Override
    protected LoginResultVO doApply(String account, LoginCaseFactory.LoginContext context) throws Exception {
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> get(String account, LoginCaseFactory.LoginContext context) throws Exception {
        return verifyPasswordNode;
    }
}
