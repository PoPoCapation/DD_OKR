package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    /** 登录账号 */
    private String account;
    /** 明文密码 */
    private String password;
    /** 展示名称（可选，默认用 account） */
    private String username;
}
