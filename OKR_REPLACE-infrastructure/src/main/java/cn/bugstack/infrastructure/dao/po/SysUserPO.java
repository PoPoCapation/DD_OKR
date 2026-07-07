package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserPO {

    /** 用户ID */
    private Long id;
    /** 展示名称 */
    private String username;
    /** 登录账号，唯一 */
    private String account;
    /** 加密密码 */
    private String password;
    /** 所属部门ID */
    private Long departmentId;
    /** 直属上级用户ID（汇报关系） */
    private Long leaderUserId;
    /** 账号状态：1启用，0禁用 */
    private Integer status;
    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;
    /** 创建时间 */
    private Date createtime;
    /** 更新时间 */
    private Date updatetime;


}
