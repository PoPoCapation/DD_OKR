package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrOperationLogRepository;
import cn.bugstack.domain.activity.model.entity.OkrOperationLogVO;
import cn.bugstack.infrastructure.dao.IOkrOperationLogDao;
import cn.bugstack.infrastructure.dao.po.OkrOperationLogPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrOperationLogRepository implements IOkrOperationLogRepository {

    @Resource
    private IOkrOperationLogDao dao;

    @Override
    public void log(String serviceName, String resourceType, Long resourceId, String action,
                    Long operatorId, String beforeJson, String afterJson, String requestId, String ip) {
        OkrOperationLogPO po = OkrOperationLogPO.builder()
                .serviceName(serviceName)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .action(action)
                .operatorId(operatorId)
                .beforeJson(beforeJson)
                .afterJson(afterJson)
                .requestId(requestId)
                .ip(ip)
                .isDeleted(0)
                .createtime(new Date())
                .updatetime(new Date())
                .build();
        dao.insert(po);
    }

    @Override
    public List<OkrOperationLogVO> queryByResource(String resourceType, Long resourceId) {
        return toVOList(dao.queryByResource(resourceType, resourceId));
    }

    @Override
    public List<OkrOperationLogVO> queryByOperator(Long operatorId) {
        return toVOList(dao.queryByOperator(operatorId));
    }

    private List<OkrOperationLogVO> toVOList(List<OkrOperationLogPO> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(po -> OkrOperationLogVO.builder()
                .id(po.getId())
                .serviceName(po.getServiceName())
                .resourceType(po.getResourceType())
                .resourceId(po.getResourceId())
                .action(po.getAction())
                .operatorId(po.getOperatorId())
                .beforeJson(po.getBeforeJson())
                .afterJson(po.getAfterJson())
                .requestId(po.getRequestId())
                .ip(po.getIp())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build()).collect(Collectors.toList());
    }
}
