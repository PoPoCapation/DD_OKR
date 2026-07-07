package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.infrastructure.dao.IOkrKeyResultDao;
import cn.bugstack.infrastructure.dao.po.OkrKeyResultPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrKeyResultRepository implements IOkrKeyResultRepository {

    @Resource
    private IOkrKeyResultDao dao;

    @Override
    public boolean createKeyResult(OkrKeyResultVO vo) {
        return dao.insert(toPO(vo)) == 1;
    }

    @Override
    public boolean updateKeyResult(OkrKeyResultVO vo) {
        return dao.update(toPO(vo)) == 1;
    }

    @Override
    public boolean deleteKeyResult(Long krId) {
        return dao.delete(krId) == 1;
    }

    @Override
    public OkrKeyResultVO queryKeyResultById(Long krId) {
        OkrKeyResultPO po = dao.queryById(krId);
        return po == null ? null : toVO(po);
    }

    @Override
    public List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long objectiveId) {
        List<OkrKeyResultPO> list = dao.queryByObjectiveId(objectiveId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<OkrKeyResultVO> queryKeyResultPage(Long objectiveId, Integer offset, Integer size) {
        List<OkrKeyResultPO> list = dao.queryPageByObjectiveId(objectiveId, offset, size);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public long countByObjectiveId(Long objectiveId) {
        Long c = dao.countByObjectiveId(objectiveId);
        return c == null ? 0L : c;
    }

    private OkrKeyResultPO toPO(OkrKeyResultVO vo) {
        return OkrKeyResultPO.builder()
                .id(vo.getId())
                .krName(vo.getKrName())
                .sortOrder(vo.getSortOrder())
                .weight(vo.getWeight())
                .completionRate(vo.getCompletionRate())
                .objectiveId(vo.getObjectiveId())
                .deadline(vo.getDeadline())
                .status(vo.getStatus())
                .remark(vo.getRemark())
                .isDeleted(vo.getIsDeleted())
                .createtime(vo.getCreatetime())
                .updatetime(vo.getUpdatetime())
                .build();
    }

    private OkrKeyResultVO toVO(OkrKeyResultPO po) {
        return OkrKeyResultVO.builder()
                .id(po.getId())
                .krName(po.getKrName())
                .sortOrder(po.getSortOrder())
                .weight(po.getWeight())
                .completionRate(po.getCompletionRate())
                .objectiveId(po.getObjectiveId())
                .deadline(po.getDeadline())
                .status(po.getStatus())
                .remark(po.getRemark())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build();
    }
}
