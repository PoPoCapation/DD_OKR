package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户与Task关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrTaskUserPO {

    /** 关联主键ID */
    private Long id;
    /** 关联用户 */
    private Long userId;
    /** 关联任务 */
    private Long taskId;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
