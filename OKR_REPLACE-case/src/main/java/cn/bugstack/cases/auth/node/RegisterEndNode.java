package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点5（链尾）：组装注册结果（注册即登录）
 */
@Slf4j
@Service("RegisterEndNode")
public class RegisterEndNode extends AbstractRegisterCaseSupport {

    @Override
    protected LoginResultVO doApply(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
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
    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> get(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return null;
    }
}
