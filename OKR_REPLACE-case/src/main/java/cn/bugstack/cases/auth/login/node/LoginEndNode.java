package cn.bugstack.cases.auth.login.node;

import cn.bugstack.cases.auth.login.factory.LoginCaseFactory;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点4（链尾）：组装登录结果返回
 */
@Slf4j
@Service("LoginEndNode")
public class LoginEndNode extends AbstractLoginCaseSupport {

    @Override
    protected LoginResultVO doApply(String account, LoginCaseFactory.LoginContext context) throws Exception {
        SystemUserVO user = context.getUser();
        return LoginResultVO.builder()
                .userId(user.getId())
                .account(user.getAccount())
                .token(context.getToken())
                .roles(context.getRoleCodes())
                .permissions(context.getPermissionCodes())
                .build();
    }

    @Override
    public StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> get(String account, LoginCaseFactory.LoginContext context) throws Exception {
        return null;
    }
}
