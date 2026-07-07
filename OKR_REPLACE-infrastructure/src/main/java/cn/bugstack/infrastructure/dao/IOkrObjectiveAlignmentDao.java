package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrObjectiveAlignmentPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOkrObjectiveAlignmentDao {
    int insert(OkrObjectiveAlignmentPO po);
    OkrObjectiveAlignmentPO queryById(Long id);
    int update(OkrObjectiveAlignmentPO po);
    int delete(Long id);

    /** 出向对齐：我对齐到哪些上级O */
    List<OkrObjectiveAlignmentPO> queryByObjectiveId(Long objectiveId);

    /** 入向对齐：哪些下级O对齐到我 */
    List<OkrObjectiveAlignmentPO> queryByAlignedObjectiveId(Long alignedObjectiveId);

    /** 删除某O的所有出向对齐 */
    int deleteByObjectiveId(Long objectiveId);
}
