package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrCheckInRepository;
import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;
import cn.bugstack.domain.activity.model.entity.OkrCheckInVO;
import cn.bugstack.infrastructure.dao.IOkrCheckInDao;
import cn.bugstack.infrastructure.dao.IOkrCheckInItemDao;
import cn.bugstack.infrastructure.dao.po.OkrCheckInItemPO;
import cn.bugstack.infrastructure.dao.po.OkrCheckInPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrCheckInRepository implements IOkrCheckInRepository {

    @Resource
    private IOkrCheckInDao checkInDao;
    @Resource
    private IOkrCheckInItemDao itemDao;

    @Override
    public Long createCheckIn(OkrCheckInVO vo) {
        OkrCheckInPO po = OkrCheckInPO.builder()
                .objectiveId(vo.getObjectiveId())
                .cycleId(vo.getCycleId())
                .checkInUserId(vo.getCheckInUserId())
                .confidenceLevel(vo.getConfidenceLevel())
                .summary(vo.getSummary())
                .risk(vo.getRisk())
                .blocker(vo.getBlocker())
                .nextPlan(vo.getNextPlan())
                .applyStatus("APPLIED")
                .submittedAt(new Date())
                .isDeleted(0)
                .createtime(new Date())
                .updatetime(new Date())
                .build();
        checkInDao.insert(po);
        return po.getId();
    }

    @Override
    public List<OkrCheckInVO> queryByObjectiveId(Long objectiveId) {
        List<OkrCheckInPO> list = checkInDao.queryByObjectiveId(objectiveId);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(po -> OkrCheckInVO.builder()
                .id(po.getId())
                .objectiveId(po.getObjectiveId())
                .cycleId(po.getCycleId())
                .checkInUserId(po.getCheckInUserId())
                .confidenceLevel(po.getConfidenceLevel())
                .summary(po.getSummary())
                .risk(po.getRisk())
                .blocker(po.getBlocker())
                .nextPlan(po.getNextPlan())
                .submittedAt(po.getSubmittedAt())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build()).collect(Collectors.toList());
    }

    @Override
    public void insertItem(OkrCheckInItemVO item) {
        OkrCheckInItemPO po = OkrCheckInItemPO.builder()
                .checkInId(item.getCheckInId())
                .krId(item.getKrId())
                .oldCompletionRate(item.getOldCompletionRate())
                .newCompletionRate(item.getNewCompletionRate())
                .progressDelta(item.getProgressDelta())
                .remark(item.getRemark())
                .applyStatus("APPLIED")
                .isDeleted(0)
                .createtime(new Date())
                .updatetime(new Date())
                .build();
        itemDao.insert(po);
    }

    @Override
    public List<OkrCheckInItemVO> queryItemsByCheckInId(Long checkInId) {
        List<OkrCheckInItemPO> list = itemDao.queryByCheckInId(checkInId);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(po -> OkrCheckInItemVO.builder()
                .id(po.getId())
                .checkInId(po.getCheckInId())
                .krId(po.getKrId())
                .oldCompletionRate(po.getOldCompletionRate())
                .newCompletionRate(po.getNewCompletionRate())
                .progressDelta(po.getProgressDelta())
                .remark(po.getRemark())
                .applyStatus(po.getApplyStatus())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build()).collect(Collectors.toList());
    }
}
