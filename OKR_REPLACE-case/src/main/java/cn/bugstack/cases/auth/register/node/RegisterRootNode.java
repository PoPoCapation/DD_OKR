package cn.bugstack.cases.auth.register.node;

import cn.bugstack.cases.auth.register.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("RegisterRootNode")
public class RegisterRootNode extends AbstractRegisterCaseSupport {

    @Resource(name = "RegisterCheckExistNode")
    private RegisterCheckExistNode checkExistNode;

    @Override
    protected LoginResultVO doApply(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> get(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return checkExistNode;
    }
}
