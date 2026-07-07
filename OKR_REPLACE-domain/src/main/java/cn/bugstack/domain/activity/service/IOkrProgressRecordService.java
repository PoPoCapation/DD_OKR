package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrProgressRecordVO;

import java.math.BigDecimal;
import java.util.List;

public interface IOkrProgressRecordService {

    /** 记录进度变更 */
    void recordChange(String targetType, Long targetId, BigDecimal oldProgress,
                      BigDecimal newProgress, String sourceType, Long operatorId, String remark);

    /** 按目标查询记录 */
    List<OkrProgressRecordVO> queryByTarget(String targetType, Long targetId);

    /** 按操作人查询记录 */
    List<OkrProgressRecordVO> queryByOperator(Long operatorId);
}
