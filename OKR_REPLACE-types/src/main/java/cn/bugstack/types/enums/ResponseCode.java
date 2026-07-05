package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    USER_CREATE_FAIL("0003" ,"创建用户失败"),
    USER_UPDATE_FAIL("0004" ,"更新用户失败"),
    USER_DELETE_FAIL("0005" ,"删除用户失败"),
    USER_FIND_FAIL("0006" ,"查询用户失败"),
    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),

    ;

    private String code;
    private String info;

}
