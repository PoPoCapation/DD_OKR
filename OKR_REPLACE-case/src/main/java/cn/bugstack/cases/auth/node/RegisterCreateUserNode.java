package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 节点2：创建用户（UserService.createUser 内部 BCrypt 加密），再查回拿 userId
 */
@Slf4j
@Service("RegisterCreateUserNode")
public class RegisterCreateUserNode extends AbstractRegisterCaseSupport {

    @Resource(name = "RegisterLoadPermissionNode")
    private RegisterLoadPermissionNode loadPermissionNode;

    @Override
    protected LoginResultVO doApply(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        String username = (context.getUsername() == null || context.getUsername().isEmpty()) ? account : context.getUsername();
        SystemUserVO newUser = SystemUserVO.builder()
                .username(username)
                .account(account)
                .password(context.getPassword())
                .status(1)
                .isDeleted(0)
                .createtime(new Date())
                .updatetime(new Date())
                .build();
        userService.createUser(newUser);
        // createUser 不回填 id 到 VO，查回拿 userId
        context.setUser(userRepository.queryUserByAccount(account));
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> get(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return loadPermissionNode;
    }
}
