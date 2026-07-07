package cn.bugstack.cases.auth.register.node;

import cn.bugstack.cases.auth.register.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点1：账号查重（已存在则抛 ACCOUNT_EXISTS）
 */
@Slf4j
@Service("RegisterCheckExistNode")
public class RegisterCheckExistNode extends AbstractRegisterCaseSupport {

    @Resource(name = "RegisterCreateUserNode")
    private RegisterCreateUserNode createUserNode;

    @Override
    protected LoginResultVO doApply(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        if (userRepository.queryUserByAccount(account) != null) {
            throw new AppException(ResponseCode.ACCOUNT_EXISTS.getCode(), ResponseCode.ACCOUNT_EXISTS.getInfo());
        }
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> get(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return createUserNode;
    }
}
