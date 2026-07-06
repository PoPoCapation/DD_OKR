package cn.bugstack.cases.auth.node;

import cn.bugstack.cases.auth.factory.RegisterCaseFactory;
import cn.bugstack.domain.activity.JWT;
import cn.bugstack.domain.user.adapter.repository.IUserRepository;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.domain.user.service.IUserRoleService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 注册节点基类
 */
public abstract class AbstractRegisterCaseSupport extends AbstractMultiThreadStrategyRouter<String, RegisterCaseFactory.RegisterContext, LoginResultVO> {

    @Resource
    protected IUserRepository userRepository;
    @Resource
    protected IUserService userService;
    @Resource
    protected IUserRoleService userRoleService;
    @Resource
    protected JWT jwt;

    @Override
    protected void multiThread(String requestParameter, RegisterCaseFactory.RegisterContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
    }
}
