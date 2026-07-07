package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrProgressRecordRepository;
import cn.bugstack.domain.activity.model.entity.OkrProgressRecordVO;
import cn.bugstack.domain.activity.service.IOkrProgressRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OkrProgressRecordService implements IOkrProgressRecordService {

    @Resource
    private IOkrProgressRecordRepository repository;

    @Override
    public void recordChange(String targetType, Long targetId, BigDecimal oldProgress,
                             BigDecimal newProgress, String sourceType, Long operatorId, String remark) {
        log.info("记录进度变更: targetType={}, targetId={}, {}->{}, operator={}", targetType, targetId, oldProgress, newProgress, operatorId);
        repository.recordChange(targetType, targetId, oldProgress, newProgress, sourceType, operatorId, remark);
    }

    @Override
    public List<OkrProgressRecordVO> queryByTarget(String targetType, Long targetId) {
        return repository.queryByTarget(targetType, targetId);
    }

    @Override
    public List<OkrProgressRecordVO> queryByOperator(Long operatorId) {
        return repository.queryByOperator(operatorId);
    }
}
