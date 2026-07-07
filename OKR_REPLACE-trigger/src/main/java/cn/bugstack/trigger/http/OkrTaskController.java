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
 * Task（任务）接口
 * <p>
 * 编排策略：create/list 走 case 编排（含权限校验节点），update/delete/assign/departmentTasks/allTasks 走 domain service。
 * 所有写操作均透传 currentUserId，用于数据权限校验与审计（进度流水 / 操作日志）。
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

    /**
     * 创建任务（需对关联 KR 的父目标具备可编辑权限）。
     */
    @PostMapping("/create")
    public Response<Void> create(HttpServletRequest request, @Valid @RequestBody OkrTaskRequestDTO dto) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        taskCreateCase.createTask(userId, toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 更新任务（状态变更会写 TASK 进度流水 + 操作日志）。
     */
    @PostMapping("/update")
    public Response<Void> update(HttpServletRequest request, @Valid @RequestBody OkrTaskRequestDTO dto) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        taskService.updateTask(userId, toVO(dto));
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 删除任务（逻辑删除 + 操作日志）。
     */
    @PostMapping("/delete")
    public Response<Void> delete(HttpServletRequest request, @RequestParam("taskId") Long taskId) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        taskService.deleteTask(userId, taskId);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 查询某 KR 下的全部任务（校验可见性）。
     */
    @PostMapping("/list")
    public Response<List<OkrTaskResponseDTO>> list(HttpServletRequest request, @RequestParam("krId") Long krId) {
        Long userId = currentUserId(request);
        List<OkrTaskVO> list = taskQueryCase.queryTaskList(userId, krId);
        List<OkrTaskResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrTaskResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    /**
     * 指派任务给用户（全删全插 + 操作日志）。
     */
    @PostMapping("/assignUsers")
    public Response<Void> assignUsers(HttpServletRequest request, @RequestParam("taskId") Long taskId, @RequestBody List<Long> userIds) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Response.<Void>builder().code(ResponseCode.UNAUTHORIZED.getCode()).info(ResponseCode.UNAUTHORIZED.getInfo()).build();
        }
        taskService.assignUsers(userId, taskId, userIds);
        return Response.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).build();
    }

    /**
     * 查询我的任务（当前用户被指派的任务）。
     */
    @PostMapping("/myTasks")
    public Response<List<OkrTaskResponseDTO>> myTasks(HttpServletRequest request) {
        Long userId = currentUserId(request);
        List<OkrTaskVO> list = taskService.myTasks(userId);
        List<OkrTaskResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrTaskResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    /**
     * 查询当前用户所在部门的任务。
     */
    @PostMapping("/departmentTasks")
    public Response<List<OkrTaskResponseDTO>> departmentTasks(HttpServletRequest request) {
        Long userId = currentUserId(request);
        List<OkrTaskVO> list = taskService.queryDepartmentTasks(userId);
        List<OkrTaskResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrTaskResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    /**
     * 查询所有任务（管理视图）。
     */
    @PostMapping("/allTasks")
    public Response<List<OkrTaskResponseDTO>> allTasks(HttpServletRequest request) {
        currentUserId(request); // 仅校验登录
        List<OkrTaskVO> list = taskService.queryAllTasks();
        List<OkrTaskResponseDTO> data = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.<List<OkrTaskResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
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
