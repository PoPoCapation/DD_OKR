package cn.bugstack.trigger.listener;

import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把 AppException 与未捕获异常统一转成标准 Response
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：携带错误码返回 */
    @ExceptionHandler(AppException.class)
    public Response<?> handleAppException(AppException e) {
        log.warn("业务异常: code={}, info={}", e.getCode(), e.getInfo());
        return Response.<Object>builder()
                .code(e.getCode())
                .info(e.getInfo())
                .build();
    }

    /** 其它未捕获异常：兜底为未知失败 */
    @ExceptionHandler(Exception.class)
    public Response<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Response.<Object>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(ResponseCode.UN_ERROR.getInfo())
                .build();
    }
}
