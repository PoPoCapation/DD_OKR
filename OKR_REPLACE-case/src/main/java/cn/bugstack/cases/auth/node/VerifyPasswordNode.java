package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.LoginCaseFactory;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.types.common.PasswordEncoder;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点1：查用户 + 验密码 + 验状态
 */
@Slf4j
@Service("LoginVerifyPasswordNode")
public class VerifyPasswordNode extends AbstractLoginCaseSupport {

    @Resource(name = "LoginLoadPermissionNode")
    private LoadPermissionNode loadPermissionNode;

    @Override
    protected LoginResultVO doApply(String account, LoginCaseFactory.LoginContext context) throws Exception {
        SystemUserVO user = userService.queryUserByAccount(account);
        if (!PasswordEncoder.matches(context.getPassword(), user.getPassword())) {
            throw new AppException(ResponseCode.PASSWORD_ERROR.getCode(), ResponseCode.PASSWORD_ERROR.getInfo());
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new AppException(ResponseCode.ACCOUNT_DISABLED.getCode(), ResponseCode.ACCOUNT_DISABLED.getInfo());
        }
        context.setUser(user);
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> get(String account, LoginCaseFactory.LoginContext context) throws Exception {
        return loadPermissionNode;
    }
}
