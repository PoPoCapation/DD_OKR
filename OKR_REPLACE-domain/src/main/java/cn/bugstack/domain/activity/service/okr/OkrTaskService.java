package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskUserRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.domain.activity.service.IOkrAuditService;
import cn.bugstack.domain.activity.service.IOkrTaskService;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ProgressTargetType;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * OKR 任务（Task）领域服务
 * <p>
 * 职责：
 * 1. Task 的 CRUD（创建/更新/删除/按KR查询/部门查询/全部查询）
 * 2. 任务指派（assignUsers）—— 全删全插模式
 * 3. 「我的任务」查询（myTasks）—— 通过 okr_task_user 关联表反查
 * 4. 数据权限 —— 创建时校验 KR 所属目标在可编辑范围内；查询时校验可见性
 * 5. 审计 —— 状态变更写 TASK 进度流水；增删改/指派写操作日志
 * <p>
 * 层级关系：O（目标）→ KR（关键结果）→ Task（任务）
 *
 * @see IOkrTaskService 接口定义
 */
@Slf4j
@Service
public class OkrTaskService implements IOkrTaskService {

    private static final String SOURCE_TASK_UPDATE = "TASK_UPDATE";
    private static final String RESOURCE_TASK = "TASK";

    /** Task Repository —— 封装 okr_task 表的增删改查 */
    @Resource
    private IOkrTaskRepository repository;

    /** KR Repository —— 查询 KR 信息（获取其所属 objectiveId，用于权限校验链路） */
    @Resource
    private IOkrKeyResultRepository keyResultRepository;

    /** 目标 Repository —— 查询 O 信息（获取 ownerUserId/departmentId，判断可编辑/可见） */
    @Resource
    private IOkrObjectiveRepository objectiveRepository;

    /** 用户服务 —— 获取可见/可编辑用户列表、当前用户部门 */
    @Resource
    private IUserService userService;

    /** Task-User 关联 Repository —— 维护 okr_task_user 表（任务指派/我的任务） */
    @Resource
    private IOkrTaskUserRepository taskUserRepository;

    /** 审计服务 —— 写 TASK 进度流水与操作日志 */
    @Resource
    private IOkrAuditService auditService;

    /**
     * 创建任务
     * <p>
     * 业务逻辑：
     * 1. 校验 KR 存在，且其所属目标在当前用户的可编辑范围内（自己或下级的目标）
     * 2. 设置默认值（status=todo、priority=2、isDeleted=0、时间）；departmentId 取自父目标
     * 3. 持久化，失败抛 OKR_TASK_CREATE_FAIL
     * 4. 写操作日志（CREATE）
     *
     * @param currentUserId 操作人
     * @param vo            任务信息（taskName + krId 必填）
     */
    @Override
    @Transactional
    public void createTask(Long currentUserId, OkrTaskVO vo) {
        log.info("开始创建Task: taskName = {}, krId = {}, currentUserId = {}", vo.getTaskName(), vo.getKrId(), currentUserId);
        OkrObjectiveVO objective = loadAndCheckEditable(currentUserId, vo.getKrId());

        // 默认值
        if (vo.getStatus() == null) vo.setStatus("todo");
        if (vo.getPriority() == null) vo.setPriority(2);
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);
        vo.setDepartmentId(objective.getDepartmentId());
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());

        if (!repository.createTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_CREATE_FAIL.getCode(), ResponseCode.OKR_TASK_CREATE_FAIL.getInfo());
        }
        auditService.recordOperation("OkrTaskService", RESOURCE_TASK, vo.getId(), "CREATE", currentUserId, null, vo);
    }

    /**
     * 更新任务
     * <p>
     * 业务逻辑：
     * 1. 查旧任务（记旧状态，用于状态变更流水与日志前后快照）
     * 2. 动态更新，失败抛 OKR_TASK_UPDATE_FAIL
     * 3. 若 status 发生变化：写 TASK 进度流水（old→new，按状态映射进度值）
     * 4. 写操作日志（UPDATE，前后快照）
     *
     * @param currentUserId 操作人
     * @param vo            任务信息（id 必填）
     */
    @Override
    @Transactional
    public void updateTask(Long currentUserId, OkrTaskVO vo) {
        log.info("开始更新Task: id = {}, currentUserId = {}", vo.getId(), currentUserId);
        OkrTaskVO before = repository.queryTaskById(vo.getId());
        if (before == null) {
            throw new AppException(ResponseCode.OKR_TASK_FIND_FAIL.getCode(), ResponseCode.OKR_TASK_FIND_FAIL.getInfo());
        }
        vo.setUpdatetime(new Date());
        if (!repository.updateTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_UPDATE_FAIL.getCode(), ResponseCode.OKR_TASK_UPDATE_FAIL.getInfo());
        }

        // 状态变更 → 写 TASK 进度流水（按状态映射进度值）
        if (vo.getStatus() != null && !vo.getStatus().equals(before.getStatus())) {
            BigDecimal oldProgress = statusToProgress(before.getStatus());
            BigDecimal newProgress = statusToProgress(vo.getStatus());
            auditService.recordProgress(ProgressTargetType.TASK.code(), vo.getId(), oldProgress, newProgress,
                    SOURCE_TASK_UPDATE, currentUserId, "状态变更: " + before.getStatus() + "->" + vo.getStatus());
        }
        OkrTaskVO after = repository.queryTaskById(vo.getId());
        auditService.recordOperation("OkrTaskService", RESOURCE_TASK, vo.getId(), "UPDATE", currentUserId, before, after);
    }

    /**
     * 删除任务（逻辑删除）
     */
    @Override
    @Transactional
    public void deleteTask(Long currentUserId, Long taskId) {
        log.info("开始删除Task: id = {}, currentUserId = {}", taskId, currentUserId);
        OkrTaskVO before = repository.queryTaskById(taskId);
        if (before == null) {
            throw new AppException(ResponseCode.OKR_TASK_FIND_FAIL.getCode(), ResponseCode.OKR_TASK_FIND_FAIL.getInfo());
        }
        if (!repository.deleteTask(taskId)) {
            throw new AppException(ResponseCode.OKR_TASK_DELETE_FAIL.getCode(), ResponseCode.OKR_TASK_DELETE_FAIL.getInfo());
        }
        auditService.recordOperation("OkrTaskService", RESOURCE_TASK, taskId, "DELETE", currentUserId, before, null);
    }

    /**
     * 查询某 KR 下的所有任务（带数据权限校验：Task→KR→O→owner 可见性）
     */
    @Override
    public List<OkrTaskVO> queryTaskListByKrId(Long currentUserId, Long krId) {
        log.info("开始查询Task列表: currentUserId = {}, krId = {}", currentUserId, krId);
        checkVisibleByKr(currentUserId, krId);
        return repository.queryTaskListByKrId(krId);
    }

    /**
     * 指派任务给用户（全删全插模式）+ 操作日志。
     */
    @Override
    @Transactional
    public void assignUsers(Long currentUserId, Long taskId, List<Long> userIds) {
        log.info("开始指派任务: taskId = {}, userIds = {}, currentUserId = {}", taskId, userIds, currentUserId);
        List<Long> before = taskUserRepository.queryUserIdsByTaskId(taskId);
        taskUserRepository.assignUsers(taskId, userIds);
        auditService.recordOperation("OkrTaskService", RESOURCE_TASK, taskId, "ASSIGN", currentUserId, before, userIds);
    }

    @Override
    public List<OkrTaskVO> myTasks(Long currentUserId) {
        log.info("开始查询我的任务: currentUserId = {}", currentUserId);
        List<Long> taskIds = taskUserRepository.queryTaskIdsByUserId(currentUserId);
        return repository.queryTasksByTaskIds(taskIds);
    }

    /**
     * 查询当前用户所在部门的任务（按 department_id 过滤）。
     */
    @Override
    public List<OkrTaskVO> queryDepartmentTasks(Long currentUserId) {
        log.info("开始查询部门任务: currentUserId = {}", currentUserId);
        SystemUserVO user = userService.queryUserByUserId(currentUserId);
        if (user == null || user.getDepartmentId() == null) {
            return java.util.Collections.emptyList();
        }
        return repository.queryTaskListByDepartmentId(user.getDepartmentId());
    }

    /**
     * 查询所有任务（管理视图）。
     */
    @Override
    public List<OkrTaskVO> queryAllTasks() {
        log.info("开始查询所有任务");
        return repository.queryAllTasks();
    }

    // ==================== 内部辅助 ====================

    /** 加载 KR→O 并校验当前用户对父目标的可编辑权限，返回父目标 */
    private OkrObjectiveVO loadAndCheckEditable(Long currentUserId, Long krId) {
        OkrKeyResultVO kr = keyResultRepository.queryKeyResultById(krId);
        if (kr == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(kr.getObjectiveId());
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> editableUserIds = userService.queryEditableUserIds(currentUserId);
        if (!editableUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
        return objective;
    }

    /** 校验当前用户对 KR 所属目标的可见性 */
    private void checkVisibleByKr(Long currentUserId, Long krId) {
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
    }

    /** 任务状态 → 进度值映射（用于 TASK 维度进度流水） */
    private BigDecimal statusToProgress(String status) {
        if (status == null) return BigDecimal.ZERO;
        switch (status) {
            case "done": return new BigDecimal("100");
            case "ongoing": return new BigDecimal("50");
            case "cancel":
            case "todo":
            default: return BigDecimal.ZERO;
        }
    }
}
