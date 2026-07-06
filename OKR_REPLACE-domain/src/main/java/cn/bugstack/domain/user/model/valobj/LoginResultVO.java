package cn.bugstack.domain.user.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResultVO {
    /** 用户ID */
    private Long userId;
    /** 登录账号 */
    private String account;
    /** JWT token */
    private String token;
    /** 角色编码列表 */
    private List<String> roles;
    /** 权限编码列表 */
    private List<String> permissions;
}
