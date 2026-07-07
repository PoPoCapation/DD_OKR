package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.service.IOkrAuditService;
import cn.bugstack.domain.activity.service.IOkrOperationLogService;
import cn.bugstack.domain.activity.service.IOkrProgressRecordService;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * OKR 审计服务实现
 * <p>
 * 将「进度记录」与「操作日志」两条审计写入收口在此处，业务服务只需传入语义化参数，
 * 由本服务负责 JSON 序列化（before/after 快照）与下沉调用。
 * <p>
 * 注意：本服务不独立开启事务，依赖调用方（各业务 service 的 {@code @Transactional} 方法）
 * 提供事务上下文，从而保证「审计写入与业务变更同事务」。
 */
@Slf4j
@Service
public class OkrAuditService implements IOkrAuditService {

    @Resource
    private IOkrProgressRecordService progressRecordService;

    @Resource
    private IOkrOperationLogService operationLogService;

    @Override
    public void recordProgress(String targetType, Long targetId, BigDecimal oldProgress, BigDecimal newProgress,
                               String sourceType, Long operatorId, String remark) {
        try {
            progressRecordService.recordChange(targetType, targetId, oldProgress, newProgress, sourceType, operatorId, remark);
        } catch (Exception e) {
            // 审计失败不应阻断主流程已提交的业务，但需可观测；同事务下若业务回滚则审计一并回滚
            log.error("记录进度流水失败 targetType={}, targetId={}, {}->{}", targetType, targetId, oldProgress, newProgress, e);
        }
    }

    @Override
    public void recordOperation(String serviceName, String resourceType, Long resourceId, String action,
                                Long operatorId, Object before, Object after) {
        try {
            String beforeJson = before == null ? null : JSON.toJSONString(before);
            String afterJson = after == null ? null : JSON.toJSONString(after);
            // requestId / ip 暂留空，可后续由请求上下文补齐
            operationLogService.log(serviceName, resourceType, resourceId, action, operatorId,
                    beforeJson, afterJson, null, null);
        } catch (Exception e) {
            log.error("记录操作日志失败 service={}, resource={}/{}, action={}", serviceName, resourceType, resourceId, action, e);
        }
    }
}
