package cn.bugstack.types.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具（BCrypt）
 * <p>
 * 创建用户时调用 {@link #encode} 加密明文密码后存储；
 * 登录时调用 {@link #matches} 校验明文与库中密文是否匹配。
 */
public final class PasswordEncoder {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordEncoder() {
    }

    /** 加密明文密码 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验明文与密文是否匹配 */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encodedPassword != null && ENCODER.matches(rawPassword, encodedPassword);
    }
}
