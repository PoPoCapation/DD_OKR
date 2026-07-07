package cn.bugstack.cases.auth.register.node;

import cn.bugstack.cases.auth.register.factory.RegisterCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点3：加载角色编码 + 权限编码（新用户默认为空）
 */
@Slf4j
@Service("RegisterLoadPermissionNode")
public class RegisterLoadPermissionNode extends AbstractRegisterCaseSupport {

    @Resource(name = "RegisterSignTokenNode")
    private RegisterSignTokenNode signTokenNode;

    @Override
    protected LoginResultVO doApply(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        Long userId = context.getUser().getId();
        context.setRoleCodes(userRoleService.queryRoleCodesByUserId(userId));
        context.setPermissionCodes(userRoleService.queryPermissionCodesByUserId(userId));
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, RegisterCaseFactory.RegisterContext, LoginResultVO> get(String account, RegisterCaseFactory.RegisterContext context) throws Exception {
        return signTokenNode;
    }
}
