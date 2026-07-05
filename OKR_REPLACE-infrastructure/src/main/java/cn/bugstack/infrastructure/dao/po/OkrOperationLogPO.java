package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * OKR操作日志表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OkrOperationLogPO {

    /** 主键ID */
    private Long id;
    /** 服务名 */
    private String serviceName;
    /** OBJECTIVE/KR/TASK/CYCLE/USER/DEPARTMENT */
    private String resourceType;
    /** 资源ID */
    private Long resourceId;
    /** CREATE/UPDATE/DELETE/STATUS_CHANGE/LOGIN */
    private String action;
    /** 操作人ID */
    private Long operatorId;
    /** 操作前数据（JSON） */
    private String beforeJson;
    /** 操作后数据（JSON） */
    private String afterJson;
    /** 请求ID */
    private String requestId;
    /** IP地址 */
    private String ip;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;
}
