package cn.bugstack.trigger.http;

import cn.bugstack.api.dto.OkrTaskRequestDTO;
import cn.bugstack.api.dto.OkrTaskResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.cases.okr.taskcreate.ITaskCreateCase;
import cn.bugstack.cases.okr.taskquery.ITaskQueryCase;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.domain.activity.service.IOkrTaskService;
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
 * Task（任务）接口：create/list 走 case 编排，update/delete 走 service
 */
@Slf4j
@RestController
@RequestMapping("/api/okr/task")
public class OkrTaskController {

    @Resource
    private IOkrTaskService taskService;
    @Resource
    private ITaskCreateCase taskCreateCase;
    @Resource
    private ITaskQueryCase taskQueryCase;

    @PostMapping("/create")
    public Response<Void> create(@RequestBody OkrTaskRequestDTO dto) {
        taskCreateCase.createTask(toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/update")
    public Response<Void> update(@RequestBody OkrTaskRequestDTO dto) {
        taskService.updateTask(toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/delete")
    public Response<Void> delete(@RequestParam Long taskId) {
        taskService.deleteTask(taskId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    @PostMapping("/list")
    public Response<List<OkrTaskResponseDTO>> list(HttpServletRequest request, @RequestParam Long krId) {
        Long userId = (Long) request.getAttribute("userId");
        List<OkrTaskVO> list = taskQueryCase.queryTaskList(userId, krId);
        List<OkrTaskResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrTaskResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    private OkrTaskVO toVO(OkrTaskRequestDTO dto) {
        return OkrTaskVO.builder()
                .id(dto.getId())
                .taskName(dto.getTaskName())
                .status(dto.getStatus())
                .ownerUserId(dto.getOwnerUserId())
                .krId(dto.getKrId())
                .departmentId(dto.getDepartmentId())
                .priority(dto.getPriority())
                .deadline(dto.getDeadline())
                .remark(dto.getRemark())
                .build();
    }

    private OkrTaskResponseDTO toDTO(OkrTaskVO vo) {
        return OkrTaskResponseDTO.builder()
                .id(vo.getId())
                .taskName(vo.getTaskName())
                .status(vo.getStatus())
                .ownerUserId(vo.getOwnerUserId())
                .krId(vo.getKrId())
                .departmentId(vo.getDepartmentId())
                .priority(vo.getPriority())
                .deadline(vo.getDeadline())
                .remark(vo.getRemark())
                .createtime(vo.getCreatetime())
                .updatetime(vo.getUpdatetime())
                .build();
    }
}
