package cn.bugstack.cases.auth.register.node;

import cn.bugstack.cases.auth.register.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点4：签发 JWT
 */
@Slf4j
@Service("RegisterSignTokenNode")
public class RegisterSignTokenNode extends AbstractRegisterCaseSupport {

    @Resource(name = "RegisterEndNode")
    private RegisterEndNode endNode;

    @Override
    protected LoginResultVO doApply(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        SystemUserVO user = context.getUser();
        String token = jwt.createToken(user.getId(), user.getAccount(), context.getRoleCodes(), context.getPermissionCodes());
        context.setToken(token);
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> get(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return endNode;
    }
}
