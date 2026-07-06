package cn.bugstack.domain.user.service.auth;

import cn.bugstack.domain.activity.JWT;
import cn.bugstack.domain.user.adapter.repository.IUserRepository;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.domain.user.service.IAuthService;
import cn.bugstack.domain.user.service.IUserRoleService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.common.PasswordEncoder;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AuthService implements IAuthService {

    @Resource
    private IUserRepository userRepository;
    @Resource
    private IUserRoleService userRoleService;
    @Resource
    private IUserService userService;
    @Resource
    private JWT jwt;

    @Override
    public LoginResultVO login(String account, String password) {
        log.info("开始登录: account = {}", account);
        // 1. 按账号查用户（repository 返回 null 不抛异常，登录要精细区分错误码）
        SystemUserVO user = userRepository.queryUserByAccount(account);
        if (null == user) {
            throw new AppException(ResponseCode.ACCOUNT_NOT_FOUND.getCode(), ResponseCode.ACCOUNT_NOT_FOUND.getInfo());
        }
        // 2. 校验密码（BCrypt）
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new AppException(ResponseCode.PASSWORD_ERROR.getCode(), ResponseCode.PASSWORD_ERROR.getInfo());
        }
        // 3. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new AppException(ResponseCode.ACCOUNT_DISABLED.getCode(), ResponseCode.ACCOUNT_DISABLED.getInfo());
        }
        // 4. 加载角色编码 + 权限编码
        List<String> roles = userRoleService.queryRoleCodesByUserId(user.getId());
        List<String> permissions = userRoleService.queryPermissionCodesByUserId(user.getId());
        // 5. 签发 JWT（token 内携带 roles 与 permissions）
        String token = jwt.createToken(user.getId(), user.getAccount(), roles, permissions);
        log.info("登录成功: account = {}, userId = {}", account, user.getId());
        return LoginResultVO.builder()
                .userId(user.getId())
                .account(user.getAccount())
                .token(token)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public LoginResultVO register(String account, String password, String username) {
        log.info("开始注册: account = {}", account);
        // 1. 账号是否已存在
        if (userRepository.queryUserByAccount(account) != null) {
            throw new AppException(ResponseCode.ACCOUNT_EXISTS.getCode(), ResponseCode.ACCOUNT_EXISTS.getInfo());
        }
        // 2. 构造用户并创建（UserService.createUser 内部 BCrypt 加密密码）
        SystemUserVO newUser = SystemUserVO.builder()
                .username(username != null && !username.isEmpty() ? username : account)
                .account(account)
                .password(password)
                .status(1)
                .isDeleted(0)
                .createtime(new Date())
                .updatetime(new Date())
                .build();
        userService.createUser(newUser);
        // 3. 查回用户拿 userId（createUser 不回填 id 到 VO）
        SystemUserVO created = userRepository.queryUserByAccount(account);
        // 4. 新用户默认无角色无权限，直接签 token（注册即登录）
        List<String> roles = userRoleService.queryRoleCodesByUserId(created.getId());
        List<String> permissions = userRoleService.queryPermissionCodesByUserId(created.getId());
        String token = jwt.createToken(created.getId(), created.getAccount(), roles, permissions);
        log.info("注册成功: account = {}, userId = {}", account, created.getId());
        return LoginResultVO.builder()
                .userId(created.getId())
                .account(created.getAccount())
                .token(token)
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
