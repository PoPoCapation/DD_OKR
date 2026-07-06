package cn.bugstack.cases.auth;

import cn.bugstack.cases.auth.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterCaseService implements IRegisterCase {

    @Resource
    private RegisterCaseFactory registerCaseFactory;

    @Override
    public LoginResultVO register(String account, String password, String username) {
        RegisterCaseFactory.RegisterContext context = RegisterCaseFactory.RegisterContext.builder()
                .password(password)
                .username(username)
                .build();
        StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> handler = registerCaseFactory.strategyHandler();
        try {
            return handler.apply(account, context);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
