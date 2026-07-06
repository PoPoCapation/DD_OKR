package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {


    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),

    USER_CREATE_FAIL("0003" ,"创建用户失败"),
    USER_UPDATE_FAIL("0004" ,"更新用户失败"),
    USER_DELETE_FAIL("0005" ,"删除用户失败"),
    USER_FIND_FAIL("0006" ,"查询用户失败"),

    USER_ROLE_SET_FAIL("0007" ,"设置用户角色失败"),
    USER_ROLE_ADD_FAIL("0008" ,"新增用户角色失败"),
    USER_ROLE_REMOVE_FAIL("0009" ,"移除用户角色失败"),
    USER_ROLE_FIND_FAIL("0010" ,"查询用户角色失败"),

    ROLE_CREATE_FAIL("0011" ,"创建角色失败"),
    ROLE_UPDATE_FAIL("0012" ,"更新角色失败"),
    ROLE_DELETE_FAIL("0013" ,"删除角色失败"),
    ROLE_FIND_FAIL("0014" ,"查询角色失败"),
    ROLE_PERMISSION_SET_FAIL("0015" ,"设置角色权限失败"),
    ROLE_PERMISSION_ADD_FAIL("0016" ,"新增角色权限失败"),
    ROLE_PERMISSION_REMOVE_FAIL("0017" ,"移除角色权限失败"),

    PERMISSION_CREATE_FAIL("0018" ,"创建权限失败"),
    PERMISSION_UPDATE_FAIL("0019" ,"更新权限失败"),
    PERMISSION_DELETE_FAIL("0020" ,"删除权限失败"),
    PERMISSION_FIND_FAIL("0021" ,"查询权限失败"),

    DEPARTMENT_CREATE_FAIL("0022" ,"创建部门失败"),
    DEPARTMENT_UPDATE_FAIL("0023" ,"更新部门失败"),
    DEPARTMENT_DELETE_FAIL("0024" ,"删除部门失败"),
    DEPARTMENT_FIND_FAIL("0025" ,"查询部门失败"),

    ACCOUNT_NOT_FOUND("0026" ,"账号不存在"),
    PASSWORD_ERROR("0027" ,"密码错误"),
    ACCOUNT_DISABLED("0028" ,"账号已禁用"),
    LOGIN_FAIL("0029" ,"登录失败"),
    UNAUTHORIZED("0030" ,"未登录或token无效"),

    ;

    private String code;
    private String info;

}