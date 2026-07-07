package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrOperationLogRepository;
import cn.bugstack.domain.activity.model.entity.OkrOperationLogVO;
import cn.bugstack.domain.activity.service.IOkrOperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OkrOperationLogService implements IOkrOperationLogService {

    @Resource
    private IOkrOperationLogRepository repository;

    @Override
    public void log(String serviceName, String resourceType, Long resourceId, String action,
                    Long operatorId, String beforeJson, String afterJson, String requestId, String ip) {
        log.info("操作日志: service={}, resource={}/{}, action={}, operator={}", serviceName, resourceType, resourceId, action, operatorId);
        repository.log(serviceName, resourceType, resourceId, action, operatorId, beforeJson, afterJson, requestId, ip);
    }

    @Override
    public List<OkrOperationLogVO> queryByResource(String resourceType, Long resourceId) {
        return repository.queryByResource(resourceType, resourceId);
    }

    @Override
    public List<OkrOperationLogVO> queryByOperator(Long operatorId) {
        return repository.queryByOperator(operatorId);
    }
}
