package cn.bugstack.trigger.http;

import cn.bugstack.api.dto.PermissionRequestDTO;
import cn.bugstack.api.dto.PermissionResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.user.model.entity.SystemPermissionVO;
import cn.bugstack.domain.user.service.IPermissionService;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 权限管理接口（需登录，受 JwtInterceptor 拦截）
 */
@Slf4j
@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    @Resource
    private IPermissionService permissionService;

    /** 创建权限 */
    @PostMapping("/create")
    public Response<Void> create(@RequestBody PermissionRequestDTO request) {
        if (request == null || request.getPermCode() == null || request.getPermName() == null) {
            return Response.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        SystemPermissionVO vo = toVO(request);
        vo.setIsDeleted(0);
        if (vo.getStatus() == null) {
            vo.setStatus(1);
        }
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());
        permissionService.createPermission(vo);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 更新权限 */
    @PostMapping("/update")
    public Response<Void> update(@RequestBody PermissionRequestDTO request) {
        if (request == null || request.getId() == null) {
            return Response.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        SystemPermissionVO vo = toVO(request);
        vo.setUpdatetime(new Date());
        permissionService.updatePermission(vo);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 删除权限（逻辑删除） */
    @PostMapping("/delete")
    public Response<Void> delete(@RequestParam(value = "permissionId", required = false) Long permissionId) {
        if (permissionId == null) {
            return Response.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        permissionService.deletePermission(permissionId);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 查询权限 */
    @PostMapping("/query")
    public Response<PermissionResponseDTO> query(@RequestParam(value = "permissionId", required = false) Long permissionId) {
        if (permissionId == null) {
            return Response.<PermissionResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        SystemPermissionVO vo = permissionService.queryPermissionByPermissionId(permissionId);
        return Response.<PermissionResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(toDTO(vo))
                .build();
    }

    private SystemPermissionVO toVO(PermissionRequestDTO dto) {
        return SystemPermissionVO.builder()
                .id(dto.getId())
                .parentId(dto.getParentId())
                .permCode(dto.getPermCode())
                .permName(dto.getPermName())
                .permType(dto.getPermType())
                .path(dto.getPath())
                .sortOrder(dto.getSortOrder())
                .status(dto.getStatus())
                .remark(dto.getRemark())
                .build();
    }

    private PermissionResponseDTO toDTO(SystemPermissionVO vo) {
        return PermissionResponseDTO.builder()
                .id(vo.getId())
                .parentId(vo.getParentId())
                .permCode(vo.getPermCode())
                .permName(vo.getPermName())
                .permType(vo.getPermType())
                .path(vo.getPath())
                .sortOrder(vo.getSortOrder())
                .status(vo.getStatus())
                .remark(vo.getRemark())
                .build();
    }
}
