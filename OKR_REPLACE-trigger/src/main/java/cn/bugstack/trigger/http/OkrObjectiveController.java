package cn.bugstack.trigger.http;

import cn.bugstack.api.dto.OkrObjectiveRequestDTO;
import cn.bugstack.api.dto.OkrObjectiveResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.cases.okr.create.ICreateObjectiveCase;
import cn.bugstack.cases.okr.query.IQueryObjectiveCase;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOKRObjectiveService;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OKR 目标接口：create/list 走 case 编排，update/delete 走 domain service
 */
@Slf4j
@RestController
@RequestMapping("/api/okr/objective")
public class OkrObjectiveController {

    @Resource
    private ICreateObjectiveCase createObjectiveCase;
    @Resource
    private IQueryObjectiveCase queryObjectiveCase;
    @Resource
    private IOKRObjectiveService objectiveService;

    @PostMapping("/create")
    public Response<Void> create(HttpServletRequest request, @RequestBody OkrObjectiveRequestDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        createObjectiveCase.createObjective(userId, toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/update")
    public Response<Void> update(HttpServletRequest request, @RequestBody OkrObjectiveRequestDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        objectiveService.updateObjective(userId, toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/delete")
    public Response<Void> delete(HttpServletRequest request, @RequestParam("objectiveId") Long objectiveId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        objectiveService.deleteObjective(userId, objectiveId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/list")
    public Response<List<OkrObjectiveResponseDTO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Response.<List<OkrObjectiveResponseDTO>>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        List<OkrObjectiveVO> list = queryObjectiveCase.queryObjectiveList(userId);
        List<OkrObjectiveResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrObjectiveResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    private OkrObjectiveVO toVO(OkrObjectiveRequestDTO dto) {
        return OkrObjectiveVO.builder()
                .id(dto.getId())
                .objectiveName(dto.getObjectiveName())
                .cycleId(dto.getCycleId())
                .progress(dto.getProgress())
                .status(dto.getStatus())
                .remark(dto.getRemark())
                .build();
    }

    private OkrObjectiveResponseDTO toDTO(OkrObjectiveVO vo) {
        return OkrObjectiveResponseDTO.builder()
                .id(vo.getId())
                .objectiveName(vo.getObjectiveName())
                .ownerUserId(vo.getOwnerUserId())
                .departmentId(vo.getDepartmentId())
                .cycleId(vo.getCycleId())
                .progress(vo.getProgress())
                .status(vo.getStatus())
                .remark(vo.getRemark())
                .createtime(vo.getCreatetime())
                .updatetime(vo.getUpdatetime())
                .build();
    }
}
