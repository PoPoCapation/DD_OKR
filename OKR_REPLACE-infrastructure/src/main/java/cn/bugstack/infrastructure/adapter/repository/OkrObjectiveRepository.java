package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.infrastructure.dao.IOkrObjectiveDao;
import cn.bugstack.infrastructure.dao.po.OkrObjectivePO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrObjectiveRepository implements IOkrObjectiveRepository {

    @Resource
    private IOkrObjectiveDao okrObjectiveDao;

    @Override
    public boolean createObjective(OkrObjectiveVO vo) {
        return okrObjectiveDao.insert(toPO(vo)) == 1;
    }

    @Override
    public boolean updateObjective(OkrObjectiveVO vo) {
        return okrObjectiveDao.update(toPO(vo)) == 1;
    }

    @Override
    public boolean deleteObjective(Long id) {
        return okrObjectiveDao.delete(id) == 1;
    }

    @Override
    public OkrObjectiveVO queryObjectiveById(Long id) {
        OkrObjectivePO po = okrObjectiveDao.queryById(id);
        return po == null ? null : toVO(po);
    }

    @Override
    public List<OkrObjectiveVO> queryObjectiveList(String dataScope, Long userId, Long deptId, List<Long> deptIds) {
        List<OkrObjectivePO> list = okrObjectiveDao.queryList(dataScope, userId, deptId, deptIds);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<OkrObjectiveVO> queryObjectiveListByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<OkrObjectivePO> list = okrObjectiveDao.queryListByUserIds(userIds);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private OkrObjectivePO toPO(OkrObjectiveVO vo) {
        return OkrObjectivePO.builder()
                .id(vo.getId())
                .objectiveName(vo.getObjectiveName())
                .ownerUserId(vo.getOwnerUserId())
                .departmentId(vo.getDepartmentId())
                .cycleId(vo.getCycleId())
                .progress(vo.getProgress())
                .status(vo.getStatus())
                .remark(vo.getRemark())
                .isDeleted(vo.getIsDeleted())
                .createtime(vo.getCreatetime())
                .updatetime(vo.getUpdatetime())
                .build();
    }

    private OkrObjectiveVO toVO(OkrObjectivePO po) {
        return OkrObjectiveVO.builder()
                .id(po.getId())
                .objectiveName(po.getObjectiveName())
                .ownerUserId(po.getOwnerUserId())
                .departmentId(po.getDepartmentId())
                .cycleId(po.getCycleId())
                .progress(po.getProgress())
                .status(po.getStatus())
                .remark(po.getRemark())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build();
    }
}
