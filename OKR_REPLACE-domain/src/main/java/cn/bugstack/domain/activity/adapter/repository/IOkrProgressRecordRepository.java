package cn.bugstack.domain.activity.adapter.repository;

import cn.bugstack.domain.activity.model.entity.OkrProgressRecordVO;

import java.util.List;

public interface IOkrProgressRecordRepository {

    void recordChange(String targetType, Long targetId, java.math.BigDecimal oldProgress,
                      java.math.BigDecimal newProgress, String sourceType, Long operatorId, String remark);

    List<OkrProgressRecordVO> queryByTarget(String targetType, Long targetId);

    List<OkrProgressRecordVO> queryByOperator(Long operatorId);
}
