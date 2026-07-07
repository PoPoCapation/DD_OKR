package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveAlignmentRepository;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveAlignmentVO;
import cn.bugstack.infrastructure.dao.IOkrObjectiveAlignmentDao;
import cn.bugstack.infrastructure.dao.po.OkrObjectiveAlignmentPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrObjectiveAlignmentRepository implements IOkrObjectiveAlignmentRepository {

    @Resource
    private IOkrObjectiveAlignmentDao dao;

    @Override
    public boolean createAlignment(OkrObjectiveAlignmentVO vo) {
        return dao.insert(toPO(vo)) == 1;
    }

    @Override
    public boolean deleteAlignment(Long id) {
        return dao.delete(id) == 1;
    }

    @Override
    public List<OkrObjectiveAlignmentVO> findOutbound(Long objectiveId) {
        return toVOList(dao.queryByObjectiveId(objectiveId));
    }

    @Override
    public List<OkrObjectiveAlignmentVO> findInbound(Long objectiveId) {
        return toVOList(dao.queryByAlignedObjectiveId(objectiveId));
    }

    @Override
    public boolean deleteByObjectiveId(Long objectiveId) {
        return dao.deleteByObjectiveId(objectiveId) > 0;
    }

    private OkrObjectiveAlignmentPO toPO(OkrObjectiveAlignmentVO vo) {
        return OkrObjectiveAlignmentPO.builder()
                .id(vo.getId())
                .objectiveId(vo.getObjectiveId())
                .alignedObjectiveId(vo.getAlignedObjectiveId())
                .alignedKrId(vo.getAlignedKrId())
                .alignmentType(vo.getAlignmentType())
                .status(vo.getStatus())
                .createdBy(vo.getCreatedBy())
                .updatedBy(vo.getUpdatedBy())
                .isDeleted(vo.getIsDeleted())
                .build();
    }

    private OkrObjectiveAlignmentVO toVO(OkrObjectiveAlignmentPO po) {
        return OkrObjectiveAlignmentVO.builder()
                .id(po.getId())
                .objectiveId(po.getObjectiveId())
                .alignedObjectiveId(po.getAlignedObjectiveId())
                .alignedKrId(po.getAlignedKrId())
                .alignmentType(po.getAlignmentType())
                .status(po.getStatus())
                .createdBy(po.getCreatedBy())
                .updatedBy(po.getUpdatedBy())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build();
    }

    private List<OkrObjectiveAlignmentVO> toVOList(List<OkrObjectiveAlignmentPO> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
}
