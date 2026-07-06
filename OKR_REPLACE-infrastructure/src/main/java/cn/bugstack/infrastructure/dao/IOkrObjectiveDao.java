package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.OkrObjectivePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * O表 DAO
 */
@Mapper
public interface IOkrObjectiveDao {

    /** 新增Objective */
    int insert(OkrObjectivePO po);

    /** 根据ID查询Objective（不含已删除） */
    OkrObjectivePO queryById(Long id);

    /** 根据ID更新Objective */
    int update(OkrObjectivePO po);

    /** 根据ID逻辑删除Objective */
    int delete(Long id);

    /** 按数据权限查询目标列表（dataScope: all/dept_and_below/dept/self） */
    List<OkrObjectivePO> queryList(@Param("dataScope") String dataScope,
                                   @Param("userId") Long userId,
                                   @Param("deptId") Long deptId,
                                   @Param("deptIds") List<Long> deptIds);

    /** 按可见用户ID列表查询目标（汇报关系数据权限） */
    List<OkrObjectivePO> queryListByUserIds(@Param("userIds") List<Long> userIds);
}
