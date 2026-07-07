package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.domain.activity.service.IOkrTaskService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OkrTaskService implements IOkrTaskService {

    @Resource
    private IOkrTaskRepository repository;
    @Resource
    private IOkrKeyResultRepository keyResultRepository;
    @Resource
    private IOkrObjectiveRepository objectiveRepository;
    @Resource
    private IUserService userService;

    @Override
    public void createTask(OkrTaskVO vo) {
        log.info("开始创建Task: taskName = {}, krId = {}", vo.getTaskName(), vo.getKrId());
        if (!repository.createTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_CREATE_FAIL.getCode(), ResponseCode.OKR_TASK_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateTask(OkrTaskVO vo) {
        log.info("开始更新Task: id = {}", vo.getId());
        if (!repository.updateTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_UPDATE_FAIL.getCode(), ResponseCode.OKR_TASK_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteTask(Long taskId) {
        log.info("开始删除Task: id = {}", taskId);
        if (!repository.deleteTask(taskId)) {
            throw new AppException(ResponseCode.OKR_TASK_DELETE_FAIL.getCode(), ResponseCode.OKR_TASK_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public List<OkrTaskVO> queryTaskListByKrId(Long currentUserId, Long krId) {
        log.info("开始查询Task列表: currentUserId = {}, krId = {}", currentUserId, krId);
        // 数据权限:krId → KR → objectiveId → O → owner 可见
        OkrKeyResultVO kr = keyResultRepository.queryKeyResultById(krId);
        if (kr == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(kr.getObjectiveId());
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        if (!visibleUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
        return repository.queryTaskListByKrId(krId);
    }
}
