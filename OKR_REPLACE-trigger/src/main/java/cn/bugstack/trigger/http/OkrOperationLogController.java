package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.domain.activity.model.entity.OkrOperationLogVO;
import cn.bugstack.domain.activity.service.IOkrOperationLogService;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/okr/log")
public class OkrOperationLogController {

    @Resource
    private IOkrOperationLogService logService;

    @PostMapping("/queryByResource")
    public Response<List<OkrOperationLogVO>> queryByResource(@RequestParam("resourceType") String resourceType, @RequestParam("resourceId") Long resourceId) {
        return Response.<List<OkrOperationLogVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(logService.queryByResource(resourceType, resourceId))
                .build();
    }

    @PostMapping("/queryByOperator")
    public Response<List<OkrOperationLogVO>> queryByOperator(@RequestParam("operatorId") Long operatorId) {
        return Response.<List<OkrOperationLogVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(logService.queryByOperator(operatorId))
                .build();
    }
}
