package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.activity.adapter.repository.IOkrTaskUserRepository;
import cn.bugstack.infrastructure.dao.IOkrTaskUserDao;
import cn.bugstack.infrastructure.dao.po.OkrTaskUserPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OkrTaskUserRepository implements IOkrTaskUserRepository {

    @Resource
    private IOkrTaskUserDao dao;

    @Override
    public void assignUsers(Long taskId, List<Long> userIds) {
        dao.deleteByTaskId(taskId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<OkrTaskUserPO> list = userIds.stream()
                .map(uid -> OkrTaskUserPO.builder().taskId(taskId).userId(uid).build())
                .collect(Collectors.toList());
        dao.insertBatch(list);
    }

    @Override
    public List<Long> queryUserIdsByTaskId(Long taskId) {
        return safeList(dao.queryUserIdsByTaskId(taskId));
    }

    @Override
    public List<Long> queryTaskIdsByUserId(Long userId) {
        return safeList(dao.queryTaskIdsByUserId(userId));
    }

    private List<Long> safeList(List<Long> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
