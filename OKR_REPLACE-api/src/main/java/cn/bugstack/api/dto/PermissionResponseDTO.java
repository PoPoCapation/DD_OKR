package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionResponseDTO {
    private Long id;
    private Long parentId;
    private String permCode;
    private String permName;
    private String permType;
    private String path;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
