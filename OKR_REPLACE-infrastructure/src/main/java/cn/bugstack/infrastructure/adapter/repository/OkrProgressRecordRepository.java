package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrProgressRecordRepository;
import cn.bugstack.domain.activity.model.entity.OkrProgressRecordVO;
import cn.bugstack.infrastructure.dao.IOkrProgressRecordDao;
import cn.bugstack.infrastructure.dao.po.OkrProgressRecordPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrProgressRecordRepository implements IOkrProgressRecordRepository {

    @Resource
    private IOkrProgressRecordDao dao;

    @Override
    public void recordChange(String targetType, Long targetId, BigDecimal oldProgress,
                             BigDecimal newProgress, String sourceType, Long operatorId, String remark) {
        OkrProgressRecordPO po = OkrProgressRecordPO.builder()
                .targetType(targetType)
                .targetId(targetId)
                .oldProgress(oldProgress)
                .newProgress(newProgress)
                .sourceType(sourceType)
                .operatorId(operatorId)
                .remark(remark)
                .isDeleted(0)
                .createdAt(new Date())
                .build();
        dao.insert(po);
    }

    @Override
    public List<OkrProgressRecordVO> queryByTarget(String targetType, Long targetId) {
        return toVOList(dao.queryByTarget(targetType, targetId));
    }

    @Override
    public List<OkrProgressRecordVO> queryByOperator(Long operatorId) {
        return toVOList(dao.queryByOperator(operatorId));
    }

    private List<OkrProgressRecordVO> toVOList(List<OkrProgressRecordPO> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(po -> OkrProgressRecordVO.builder()
                .id(po.getId())
                .targetType(po.getTargetType())
                .targetId(po.getTargetId())
                .oldProgress(po.getOldProgress())
                .newProgress(po.getNewProgress())
                .sourceType(po.getSourceType())
                .operatorId(po.getOperatorId())
                .remark(po.getRemark())
                .isDeleted(po.getIsDeleted())
                .createdAt(po.getCreatedAt())
                .build()).collect(Collectors.toList());
    }
}
