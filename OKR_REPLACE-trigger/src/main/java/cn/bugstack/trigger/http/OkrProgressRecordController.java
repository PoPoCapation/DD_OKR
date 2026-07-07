package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.domain.activity.model.entity.OkrProgressRecordVO;
import cn.bugstack.domain.activity.service.IOkrProgressRecordService;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 进度记录接口
 */
@Slf4j
@RestController
@RequestMapping("/api/okr/progress")
public class OkrProgressRecordController {

    @Resource
    private IOkrProgressRecordService progressRecordService;

    @PostMapping("/queryByTarget")
    public Response<List<OkrProgressRecordVO>> queryByTarget(@RequestParam("targetType") String targetType, @RequestParam("targetId") Long targetId) {
        return Response.<List<OkrProgressRecordVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(progressRecordService.queryByTarget(targetType, targetId))
                .build();
    }

    @PostMapping("/queryByOperator")
    public Response<List<OkrProgressRecordVO>> queryByOperator(@RequestParam("operatorId") Long operatorId) {
        return Response.<List<OkrProgressRecordVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(progressRecordService.queryByOperator(operatorId))
                .build();
    }
}
