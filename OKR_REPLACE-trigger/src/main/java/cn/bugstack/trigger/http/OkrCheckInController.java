package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;
import cn.bugstack.domain.activity.model.entity.OkrCheckInVO;
import cn.bugstack.domain.activity.service.IOkrCheckInService;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/okr/checkin")
public class OkrCheckInController {

    @Resource
    private IOkrCheckInService checkInService;

    @PostMapping("/create")
    public Response<Long> create(HttpServletRequest request,
                                 @RequestParam("objectiveId") Long objectiveId,
                                 @RequestParam(value = "confidence", required = false) Integer confidence,
                                 @RequestParam(value = "summary", required = false) String summary,
                                 @RequestParam(value = "risk", required = false) String risk,
                                 @RequestParam(value = "blocker", required = false) String blocker,
                                 @RequestParam(value = "nextPlan", required = false) String nextPlan,
                                 @RequestBody(required = false) List<Map<String, Object>> items) {
        Long userId = (Long) request.getAttribute("userId");
        List<OkrCheckInItemVO> itemVOs = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> item : items) {
                OkrCheckInItemVO vo = new OkrCheckInItemVO();
                vo.setKrId(item.get("krId") != null ? Long.valueOf(item.get("krId").toString()) : null);
                vo.setOldCompletionRate(item.get("oldRate") != null ? new BigDecimal(item.get("oldRate").toString()) : null);
                vo.setNewCompletionRate(item.get("newRate") != null ? new BigDecimal(item.get("newRate").toString()) : null);
                vo.setRemark(item.get("remark") != null ? item.get("remark").toString() : null);
                itemVOs.add(vo);
            }
        }
        Long id = checkInService.createCheckIn(userId, objectiveId, confidence, summary, risk, blocker, nextPlan, itemVOs);
        return Response.<Long>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).data(id).build();
    }

    @PostMapping("/list")
    public Response<List<OkrCheckInVO>> list(@RequestParam("objectiveId") Long objectiveId) {
        return Response.<List<OkrCheckInVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(checkInService.queryByObjectiveId(objectiveId))
                .build();
    }

    @PostMapping("/items")
    public Response<List<OkrCheckInItemVO>> items(@RequestParam("checkInId") Long checkInId) {
        return Response.<List<OkrCheckInItemVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(checkInService.queryItems(checkInId))
                .build();
    }
}
