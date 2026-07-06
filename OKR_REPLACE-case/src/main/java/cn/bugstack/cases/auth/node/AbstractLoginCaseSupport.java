package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.LoginCaseFactory;
import cn.bugstack.domain.activity.JWT;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.domain.user.service.IUserRoleService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 登录节点基类：注入 domain service，供各节点复用
 */
public abstract class AbstractLoginCaseSupport extends AbstractMultiThreadStrategyRouter<String, LoginCaseFactory.LoginContext, LoginResultVO> {

    @Resource
    protected IUserService userService;
    @Resource
    protected IUserRoleService userRoleService;
    @Resource
    protected JWT jwt;

    @Override
    protected void multiThread(String requestParameter, LoginCaseFactory.LoginContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
    }
}
