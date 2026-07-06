package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.LoginCaseFactory;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点3：签发 JWT
 */
@Slf4j
@Service("LoginSignTokenNode")
public class SignTokenNode extends AbstractLoginCaseSupport {

    @Resource(name = "LoginEndNode")
    private LoginEndNode endNode;

    @Override
    protected LoginResultVO doApply(String account, LoginCaseFactory.LoginContext context) throws Exception {
        SystemUserVO user = context.getUser();
        String token = jwt.createToken(user.getId(), user.getAccount(), context.getRoleCodes(), context.getPermissionCodes());
        context.setToken(token);
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> get(String account, LoginCaseFactory.LoginContext context) throws Exception {
        return endNode;
    }
}
