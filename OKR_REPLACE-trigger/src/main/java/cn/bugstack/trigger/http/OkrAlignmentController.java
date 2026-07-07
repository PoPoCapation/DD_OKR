package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveAlignmentVO;
import cn.bugstack.domain.activity.service.IOkrAlignmentService;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 目标对齐接口
 */
@Slf4j
@RestController
@RequestMapping("/api/okr/alignment")
public class OkrAlignmentController {

    @Resource
    private IOkrAlignmentService alignmentService;

    @PostMapping("/link")
    public Response<Void> link(HttpServletRequest request,
                               @RequestParam("objectiveId") Long objectiveId,
                               @RequestParam("parentObjectiveId") Long parentObjectiveId) {
        Long userId = (Long) request.getAttribute("userId");
        alignmentService.linkObjectiveToParent(userId, objectiveId, parentObjectiveId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/unlink")
    public Response<Void> unlink(@RequestParam("alignmentId") Long alignmentId) {
        alignmentService.unlinkAlignment(alignmentId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/outbound")
    public Response<List<OkrObjectiveAlignmentVO>> outbound(@RequestParam("objectiveId") Long objectiveId) {
        return Response.<List<OkrObjectiveAlignmentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(alignmentService.queryOutbound(objectiveId))
                .build();
    }

    @PostMapping("/inbound")
    public Response<List<OkrObjectiveAlignmentVO>> inbound(@RequestParam("objectiveId") Long objectiveId) {
        return Response.<List<OkrObjectiveAlignmentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(alignmentService.queryInbound(objectiveId))
                .build();
    }
}
