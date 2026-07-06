package cn.bugstack.domain.activity.adapter.repository;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;

import java.util.List;

/**
 * OKR 目标 Repository
 */
public interface IOkrObjectiveRepository {

    boolean createObjective(OkrObjectiveVO vo);

    boolean updateObjective(OkrObjectiveVO vo);

    boolean deleteObjective(Long id);

    OkrObjectiveVO queryObjectiveById(Long id);

    /**
     * 按数据权限查询目标列表
     *
     * @param dataScope 数据范围 all/dept_and_below/dept/self
     * @param userId    当前用户ID（self 用）
     * @param deptId    当前用户部门ID（dept 用）
     * @param deptIds   本部门及下级部门ID列表（dept_and_below 用）
     */
    List<OkrObjectiveVO> queryObjectiveList(String dataScope, Long userId, Long deptId, List<Long> deptIds);

    /** 按可见用户ID列表查询目标（汇报关系数据权限：自己+上级+下级） */
    List<OkrObjectiveVO> queryObjectiveListByUserIds(List<Long> userIds);
}
