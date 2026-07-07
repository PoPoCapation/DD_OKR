package cn.bugstack.cases.auth.register;

import cn.bugstack.domain.user.model.valobj.LoginResultVO;

/**
 * 注册用例编排（case 层）：查重 → 创建用户 → 签 JWT（注册即登录）
 */
public interface IRegisterCase {

    LoginResultVO register(String account, String password, String username);
}
