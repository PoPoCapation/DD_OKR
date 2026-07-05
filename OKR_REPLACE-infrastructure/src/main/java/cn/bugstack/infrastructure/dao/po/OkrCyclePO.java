package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * OKR周期表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrCyclePO {

    /** 周期ID */
    private Long id;
    /** 组织/租户ID，单组织系统默认1 */
    private Long orgId;
    /** 周期所属部门ID */
    private Long departmentId;
    /** 周期类型：month, quarter, half_year, year, custom */
    private String cycleType;
    /** 周期年份，例如2026 */
    private Integer cycleYear;
    /** 周期序号：月度1-12，季度1-4，半年度1-2，年度1，自定义为空 */
    private Integer cycleNo;
    /** 周期名称，例如2026年战略目标OKR */
    private String name;
    /** 周期开始日期 */
    private Date startDate;
    /** 周期结束日期 */
    private Date endDate;
    /** 状态：draft, enabled, disabled, closed, archived */
    private String status;
    /** 创建人 */
    private Long createdBy;
    /** 创建时间 */
    private Date createtime;
    /** 更新人 */
    private Long updatedBy;
    /** 更新时间 */
    private Date updatetime;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
}
