package cn.bugstack.trigger.http;

import cn.bugstack.domain.activity.JWT;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器：校验请求头中的 token，解出 userId 放入 request 属性供后续使用
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JWT jwt;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || !jwt.verify(token)) {
            log.warn("token 校验失败: uri={}", request.getRequestURI());
            throw new AppException(ResponseCode.UNAUTHORIZED.getCode(), ResponseCode.UNAUTHORIZED.getInfo());
        }
        // 解析 userId 放入请求属性，Controller 可通过 request.getAttribute("userId") 取用
        Long userId = jwt.getUserId(token);
        request.setAttribute("userId", userId);
        return true;
    }
}
