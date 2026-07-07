package cn.bugstack.trigger.http;

import cn.bugstack.api.dto.OkrKeyResultRequestDTO;
import cn.bugstack.api.dto.OkrKeyResultResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.cases.okr.krcreate.IKrCreateCase;
import cn.bugstack.cases.okr.krquery.IKrQueryCase;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.service.IOkrKeyResultService;
import cn.bugstack.types.common.PageResult;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * KR（关键结果）接口
 * <p>
 * 编排策略：create/list 走 case 编排（含权限校验节点），update/delete/page 走 domain service。
 * 所有写操作均透传 currentUserId，用于数据权限校验与审计（进度流水 / 操作日志）。
 */
@Slf4j
@RestController
@RequestMapping("/api/okr/keyresult")
public class OkrKeyResultController {

    @Resource
    private IOkrKeyResultService keyResultService;
    @Resource
    private IKrCreateCase krCreateCase;
    @Resource
    private IKrQueryCase krQueryCase;

    /**
     * 创建 KR（需对父目标具备可编辑权限）。
     * <p>
     * 参数校验：krName 非空、objectiveId 非空、weight/completionRate 非负。
     */
    @PostMapping("/create")
    public Response<Void> create(HttpServletRequest request, @Valid @RequestBody OkrKeyResultRequestDTO dto) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        krCreateCase.createKeyResult(userId, toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 更新 KR（需对父目标具备可编辑权限；进度/权重变化会自动重算父目标并写流水）。
     */
    @PostMapping("/update")
    public Response<Void> update(HttpServletRequest request, @Valid @RequestBody OkrKeyResultRequestDTO dto) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        keyResultService.updateKeyResult(userId, toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 删除 KR（逻辑删除；删除后自动重算父目标并写流水）。
     */
    @PostMapping("/delete")
    public Response<Void> delete(HttpServletRequest request, @RequestParam("krId") Long krId) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        keyResultService.deleteKeyResult(userId, krId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 查询某目标下的全部 KR（校验当前用户对该目标的可见性）。
     */
    @PostMapping("/list")
    public Response<List<OkrKeyResultResponseDTO>> list(HttpServletRequest request, @RequestParam("objectiveId") Long objectiveId) {
        Long userId = currentUserId(request);
        List<OkrKeyResultVO> list = krQueryCase.queryKeyResultList(userId, objectiveId);
        List<OkrKeyResultResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrKeyResultResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    /**
     * 分页查询某目标下的 KR（校验可见性）。
     *
     * @param page 页码，从 1 开始，默认 1
     * @param size 每页大小，默认 10
     */
    @PostMapping("/page")
    public Response<PageResult<OkrKeyResultResponseDTO>> page(HttpServletRequest request,
                                                              @RequestParam("objectiveId") Long objectiveId,
                                                              @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                              @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        Long userId = currentUserId(request);
        PageResult<OkrKeyResultVO> result = keyResultService.queryKeyResultPage(userId, objectiveId, page, size);
        List<OkrKeyResultResponseDTO> data = result.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        PageResult<OkrKeyResultResponseDTO> dtoResult = PageResult.<OkrKeyResultResponseDTO>builder()
                .records(data).total(result.getTotal()).page(result.getPage()).size(result.getSize()).build();
        return Response.<PageResult<OkrKeyResultResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(dtoResult)
                .build();
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private OkrKeyResultVO toVO(OkrKeyResultRequestDTO dto) {
        return OkrKeyResultVO.builder()
                .id(dto.getId())
                .krName(dto.getKrName())
                .sortOrder(dto.getSortOrder())
                .weight(dto.getWeight())
                .completionRate(dto.getCompletionRate())
                .objectiveId(dto.getObjectiveId())
                .deadline(dto.getDeadline())
                .status(dto.getStatus())
                .remark(dto.getRemark())
                .build();
    }

    private OkrKeyResultResponseDTO toDTO(OkrKeyResultVO vo) {
        return OkrKeyResultResponseDTO.builder()
                .id(vo.getId())
                .krName(vo.getKrName())
                .sortOrder(vo.getSortOrder())
                .weight(vo.getWeight())
                .completionRate(vo.getCompletionRate())
                .objectiveId(vo.getObjectiveId())
                .deadline(vo.getDeadline())
                .status(vo.getStatus())
                .remark(vo.getRemark())
                .createtime(vo.getCreatetime())
                .updatetime(vo.getUpdatetime())
                .build();
    }
}
