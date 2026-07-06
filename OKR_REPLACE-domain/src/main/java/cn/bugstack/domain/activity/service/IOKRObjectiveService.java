package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;

import java.util.List;

/**
 * OKR 目标 Service
 */
public interface IOKRObjectiveService {

    /** 创建目标（ownerUserId/departmentId 取当前用户） */
    void createObjective(Long currentUserId, OkrObjectiveVO vo);

    /** 更新目标（校验数据权限） */
    void updateObjective(Long currentUserId, OkrObjectiveVO vo);

    /** 删除目标（校验数据权限） */
    void deleteObjective(Long currentUserId, Long objectiveId);

    /** 查询目标列表（按当前用户数据权限过滤） */
    List<OkrObjectiveVO> queryObjectiveList(Long currentUserId);
}
