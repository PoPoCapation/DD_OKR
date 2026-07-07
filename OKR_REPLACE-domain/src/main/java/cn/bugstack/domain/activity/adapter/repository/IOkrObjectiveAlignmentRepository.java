package cn.bugstack.domain.activity.adapter.repository;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveAlignmentVO;

import java.util.List;

public interface IOkrObjectiveAlignmentRepository {

    boolean createAlignment(OkrObjectiveAlignmentVO vo);

    boolean deleteAlignment(Long id);

    /** 出向：我对齐到哪些上级O */
    List<OkrObjectiveAlignmentVO> findOutbound(Long objectiveId);

    /** 入向：哪些下级O对齐到我 */
    List<OkrObjectiveAlignmentVO> findInbound(Long objectiveId);

    /** 删除某O的所有出向对齐 */
    boolean deleteByObjectiveId(Long objectiveId);
}
