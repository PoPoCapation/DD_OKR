package cn.bugstack.trigger.http;

import cn.bugstack.api.dto.OkrKeyResultRequestDTO;
import cn.bugstack.api.dto.OkrKeyResultResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.cases.okr.krcreate.IKrCreateCase;
import cn.bugstack.cases.okr.krquery.IKrQueryCase;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.service.IOkrKeyResultService;
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
 * KR（关键结果）接口：create/list 走 case 编排，update/delete 走 service
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

    @PostMapping("/create")
    public Response<Void> create(@RequestBody OkrKeyResultRequestDTO dto) {
        krCreateCase.createKeyResult(toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/update")
    public Response<Void> update(@RequestBody OkrKeyResultRequestDTO dto) {
        keyResultService.updateKeyResult(toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/delete")
    public Response<Void> delete(@RequestParam("krId") Long krId) {
        keyResultService.deleteKeyResult(krId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/list")
    public Response<List<OkrKeyResultResponseDTO>> list(HttpServletRequest request, @RequestParam("objectiveId") Long objectiveId) {
        Long userId = (Long) request.getAttribute("userId");
        List<OkrKeyResultVO> list = krQueryCase.queryKeyResultList(userId, objectiveId);
        List<OkrKeyResultResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrKeyResultResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
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
