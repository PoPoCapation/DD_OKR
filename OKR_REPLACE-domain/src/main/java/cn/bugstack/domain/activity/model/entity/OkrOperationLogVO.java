package cn.bugstack.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OkrOperationLogVO {
    private Long id;
    private String serviceName;
    private String resourceType;
    private Long resourceId;
    private String action;
    private Long operatorId;
    private String beforeJson;
    private String afterJson;
    private String requestId;
    private String ip;
    private Integer isDeleted;
    private Date createtime;
    private Date updatetime;
}
