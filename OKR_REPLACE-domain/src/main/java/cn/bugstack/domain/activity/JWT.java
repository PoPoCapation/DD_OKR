package cn.bugstack.domain.activity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 登录令牌工具（基于 jjwt）
 * <p>
 * 在登录流程中的角色：
 * 1. 账号密码校验通过后，调用 {@link #createToken} 签发 token 返回给前端；
 * 2. 后续请求携带 token，由拦截器/网关调用 {@link #parseToken} 或 {@link #verify} 解析校验身份。
 * <p>
 * token 内携带角色列表（roles），角色来源为 sys_user_role 关联表（一个用户可拥有多个角色）。
 * <p>
 * 可选配置（application.yml）：
 * <pre>
 * jwt:
 *   secret: xxxxx        # 签名密钥，生产环境务必覆盖默认值
 *   expire-minutes: 60   # 过期时间（分钟）
 * </pre>
 *
 * @author bugstack
 */
@Component
public class JWT {

    /** 签名密钥，生产环境务必通过配置覆盖 */
    @Value("${jwt.secret:okr_replace_default_secret_2026}")
    private String secret;

    /** token 过期时间（分钟） */
    @Value("${jwt.expire-minutes:60}")
    private Long expireMinutes;

    /**
     * 登录成功后签发 token
     *
     * @param userId  用户ID
     * @param account 登录账号
     * @param roles   用户角色编码列表（来自 sys_user_role）
     * @return JWT token
     */
    public String createToken(Long userId, String account, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("account", account)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireMinutes * 60_000L))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 解析 token；签名非法、token 过期等会抛出相应异常（ExpiredJwtException / SignatureException 等）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 token 是否有效（签名正确且未过期）
     */
    public boolean verify(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public String getAccount(String token) {
        return parseToken(token).get("account", String.class);
    }

    /**
     * 从 token 中取出角色编码列表
     */
    public List<String> getRoles(String token) {
        Object roles = parseToken(token).get("roles");
        if (roles instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object o : (List<?>) roles) {
                result.add(String.valueOf(o));
            }
            return result;
        }
        return Collections.emptyList();
    }

}
