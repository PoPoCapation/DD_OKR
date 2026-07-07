package cn.bugstack.cases.auth.login;

import cn.bugstack.cases.auth.login.factory.LoginCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginCaseService implements ILoginCase {

    @Resource
    private LoginCaseFactory loginCaseFactory;

    @Override
    public LoginResultVO login(String account, String password) {
        LoginCaseFactory.LoginContext context = new LoginCaseFactory.LoginContext();
        context.setPassword(password);
        StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> handler = loginCaseFactory.strategyHandler();
        try {
            return handler.apply(account, context);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
