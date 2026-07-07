package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrOperationLogVO;

import java.util.List;

public interface IOkrOperationLogService {
    void log(String serviceName, String resourceType, Long resourceId, String action,
             Long operatorId, String beforeJson, String afterJson, String requestId, String ip);
    List<OkrOperationLogVO> queryByResource(String resourceType, Long resourceId);
    List<OkrOperationLogVO> queryByOperator(Long operatorId);
}
