package cn.bugstack.cases.auth;

import cn.bugstack.domain.user.model.valobj.LoginResultVO;

/**
 * 登录用例编排（case 层）
 */
public interface ILoginCase {

    /** 登录：验密码 → 加载角色权限 → 签 JWT */
    LoginResultVO login(String account, String password);
}
