package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRequestDTO {
    /** 权限ID（更新时必填） */
    private Long id;
    /** 上级权限ID，0表示根节点 */
    private Long parentId;
    /** 权限编码，唯一，如 okr:objective:create */
    private String permCode;
    /** 权限名称 */
    private String permName;
    /** 权限类型：menu菜单, button按钮, api接口 */
    private String permType;
    /** 前端路由路径或接口路径 */
    private String path;
    /** 排序值，越小越靠前 */
    private Integer sortOrder;
    /** 状态：1启用，0禁用 */
    private Integer status;
    /** 备注 */
    private String remark;
}
