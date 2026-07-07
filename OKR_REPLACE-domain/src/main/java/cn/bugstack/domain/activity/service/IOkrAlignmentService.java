package cn.bugstack.domain.activity.service;

import cn.bugstack.domain.activity.model.entity.OkrObjectiveAlignmentVO;

import java.util.List;

public interface IOkrAlignmentService {

    /** 对齐目标到上级O（防环校验） */
    void linkObjectiveToParent(Long currentUserId, Long objectiveId, Long parentObjectiveId);

    /** 解除对齐 */
    void unlinkAlignment(Long alignmentId);

    /** 出向：我对齐到哪些上级O */
    List<OkrObjectiveAlignmentVO> queryOutbound(Long objectiveId);

    /** 入向：哪些下级O对齐到我 */
    List<OkrObjectiveAlignmentVO> queryInbound(Long objectiveId);
}
