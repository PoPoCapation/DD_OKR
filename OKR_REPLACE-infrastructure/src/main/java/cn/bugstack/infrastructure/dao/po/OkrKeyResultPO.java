package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * KR表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrKeyResultPO {

    /** KR主键ID */
    private Long id;
    /** KR名称 */
    private String krName;
    /** 同一个O下的排序顺序 */
    private Integer sortOrder;
    /** 权重：0-100 */
    private BigDecimal weight;
    /** 完成度：0-100 */
    private BigDecimal completionRate;
    /** 关联O ID */
    private Long objectiveId;
    /** KR截止时间 */
    private Date deadline;
    /** KR状态：todo/ongoing/done */
    private String status;
    /** 备注 */
    private String remark;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
