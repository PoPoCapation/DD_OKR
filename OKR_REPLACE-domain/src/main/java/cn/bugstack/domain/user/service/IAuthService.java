package cn.bugstack.domain.user.service;

import cn.bugstack.domain.user.model.valobj.LoginResultVO;

public interface IAuthService {
    /** 登录：校验账号密码，签发 JWT，返回 token 与角色/权限编码 */
    LoginResultVO login(String account, String password);

    /** 注册：创建账号，注册成功直接签发 token（注册即登录） */
    LoginResultVO register(String account, String password, String username);
}
