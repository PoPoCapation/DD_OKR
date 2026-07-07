package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrTaskRepository;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.infrastructure.dao.IOkrTaskDao;
import cn.bugstack.infrastructure.dao.po.OkrTaskPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrTaskRepository implements IOkrTaskRepository {

    @Resource
    private IOkrTaskDao dao;

    @Override
    public boolean createTask(OkrTaskVO vo) {
        return dao.insert(toPO(vo)) == 1;
    }

    @Override
    public boolean updateTask(OkrTaskVO vo) {
        return dao.update(toPO(vo)) == 1;
    }

    @Override
    public boolean deleteTask(Long taskId) {
        return dao.delete(taskId) == 1;
    }

    @Override
    public List<OkrTaskVO> queryTaskListByKrId(Long krId) {
        List<OkrTaskPO> list = dao.queryByKrId(krId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private OkrTaskPO toPO(OkrTaskVO vo) {
        return OkrTaskPO.builder()
                .id(vo.getId())
                .taskName(vo.getTaskName())
                .status(vo.getStatus())
                .ownerUserId(vo.getOwnerUserId())
                .krId(vo.getKrId())
                .departmentId(vo.getDepartmentId())
                .priority(vo.getPriority())
                .deadline(vo.getDeadline())
                .remark(vo.getRemark())
                .isDeleted(vo.getIsDeleted())
                .createtime(vo.getCreatetime())
                .updatetime(vo.getUpdatetime())
                .build();
    }

    private OkrTaskVO toVO(OkrTaskPO po) {
        return OkrTaskVO.builder()
                .id(po.getId())
                .taskName(po.getTaskName())
                .status(po.getStatus())
                .ownerUserId(po.getOwnerUserId())
                .krId(po.getKrId())
                .departmentId(po.getDepartmentId())
                .priority(po.getPriority())
                .deadline(po.getDeadline())
                .remark(po.getRemark())
                .isDeleted(po.getIsDeleted())
                .createtime(po.getCreatetime())
                .updatetime(po.getUpdatetime())
                .build();
    }
}
