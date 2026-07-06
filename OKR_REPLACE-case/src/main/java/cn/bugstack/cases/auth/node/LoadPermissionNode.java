package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.LoginCaseFactory;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 节点2：加载角色编码 + 权限编码
 */
@Slf4j
@Service("LoginLoadPermissionNode")
public class LoadPermissionNode extends AbstractLoginCaseSupport {

    @Resource(name = "LoginSignTokenNode")
    private SignTokenNode signTokenNode;

    @Override
    protected LoginResultVO doApply(String account, LoginCaseFactory.LoginContext context) throws Exception {
        Long userId = context.getUser().getId();
        context.setRoleCodes(userRoleService.queryRoleCodesByUserId(userId));
        context.setPermissionCodes(userRoleService.queryPermissionCodesByUserId(userId));
        return router(account, context);
    }

    @Override
    public StrategyHandler<String, LoginCaseFactory.LoginContext, LoginResultVO> get(String account, LoginCaseFactory.LoginContext context) throws Exception {
        return signTokenNode;
    }
}
