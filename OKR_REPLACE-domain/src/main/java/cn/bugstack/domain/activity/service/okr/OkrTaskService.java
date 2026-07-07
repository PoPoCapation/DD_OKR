package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrTaskUserRepository;
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

/**
 * OKR 任务（Task）领域服务
 * <p>
 * 职责：
 * 1. Task 的 CRUD（创建/更新/删除/按KR查询）
 * 2. 任务指派（assignUsers）—— 全删全插模式，先删 okr_task_user 旧关联再批量插入新的
 * 3. 「我的任务」查询（myTasks）—— 通过 okr_task_user 关联表反查当前用户被指派的任务
 * 4. 数据权限 —— 查询 Task 列表时校验当前用户对 KR→O 链路的可见性
 * <p>
 * 层级关系：O（目标）→ KR（关键结果）→ Task（任务）
 * Task 必须挂在某个 KR 下，查看 Task 需要验证用户能看对应的 KR 和 O。
 *
 * @see IOkrTaskService 接口定义
 */
@Slf4j
@Service
public class OkrTaskService implements IOkrTaskService {

    /** Task Repository —— 封装 okr_task 表的增删改查 */
    @Resource
    private IOkrTaskRepository repository;

    /** KR Repository —— 查询 KR 信息（获取其所属 objectiveId，用于权限校验链路） */
    @Resource
    private IOkrKeyResultRepository keyResultRepository;

    /** 目标 Repository —— 查询 O 信息（获取 ownerUserId，判断当前用户是否可见） */
    @Resource
    private IOkrObjectiveRepository objectiveRepository;

    /** 用户服务 —— 获取可见用户列表（数据权限校验用） */
    @Resource
    private IUserService userService;

    /** Task-User 关联 Repository —— 维护 okr_task_user 表（任务指派/我的任务） */
    @Resource
    private IOkrTaskUserRepository taskUserRepository;

    /**
     * 创建任务
     * <p>
     * 业务逻辑：调用 Repository 持久化，失败抛 OKR_TASK_CREATE_FAIL。
     * 注意：前端创建任务时需要传 krId（关联的KR ID）。
     *
     * @param vo 任务信息（taskName + krId 必填，priority/status 可选）
     */
    @Override
    public void createTask(OkrTaskVO vo) {
        log.info("开始创建Task: taskName = {}, krId = {}", vo.getTaskName(), vo.getKrId());
        if (!repository.createTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_CREATE_FAIL.getCode(), ResponseCode.OKR_TASK_CREATE_FAIL.getInfo());
        }
    }

    /**
     * 更新任务
     * <p>
     * 业务逻辑：调用 Repository 更新（动态 SQL，只更新非 null 字段），失败抛 OKR_TASK_UPDATE_FAIL。
     *
     * @param vo 任务信息（id 必填，其余为要更新的字段）
     */
    @Override
    public void updateTask(OkrTaskVO vo) {
        log.info("开始更新Task: id = {}", vo.getId());
        if (!repository.updateTask(vo)) {
            throw new AppException(ResponseCode.OKR_TASK_UPDATE_FAIL.getCode(), ResponseCode.OKR_TASK_UPDATE_FAIL.getInfo());
        }
    }

    /**
     * 删除任务（逻辑删除）
     * <p>
     * 业务逻辑：调用 Repository 逻辑删除（is_deleted=1），失败抛 OKR_TASK_DELETE_FAIL。
     *
     * @param taskId 要删除的任务ID
     */
    @Override
    public void deleteTask(Long taskId) {
        log.info("开始删除Task: id = {}", taskId);
        if (!repository.deleteTask(taskId)) {
            throw new AppException(ResponseCode.OKR_TASK_DELETE_FAIL.getCode(), ResponseCode.OKR_TASK_DELETE_FAIL.getInfo());
        }
    }

    /**
     * 查询某 KR 下的所有任务（带数据权限校验）
     * <p>
     * 权限校验链路：Task → KR → Objective → ownerUserId → 可见用户列表
     * 1. 查 KR，不存在抛 OKR_KR_FIND_FAIL
     * 2. 通过 KR.objectiveId 查父目标，不存在抛 OKR_OBJECTIVE_FIND_FAIL
     * 3. 用 {@link IUserService#queryVisibleUserIds} 获取可见用户（自己+上级+下级）
     * 4. 判断 O.ownerUserId 是否在可见范围内，不可见抛 OKR_OBJECTIVE_NO_PERMISSION
     * 5. 可见则返回该 KR 下所有任务
     *
     * @param currentUserId 当前登录用户ID
     * @param krId          KR ID
     * @return 该 KR 下的任务列表
     * @throws AppException KR/目标不存在 或 无权查看
     */
    @Override
    public List<OkrTaskVO> queryTaskListByKrId(Long currentUserId, Long krId) {
        log.info("开始查询Task列表: currentUserId = {}, krId = {}", currentUserId, krId);

        // 查 KR → 获取 objectiveId
        OkrKeyResultVO kr = keyResultRepository.queryKeyResultById(krId);
        if (kr == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }

        // 查 O → 获取 ownerUserId
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(kr.getObjectiveId());
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }

        // 数据权限：当前用户必须对该 O 可见（能看 O 才能看 O→KR→Task 链路）
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        if (!visibleUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }

        return repository.queryTaskListByKrId(krId);
    }

    /**
     * 指派任务给用户（全删全插模式）
     * <p>
     * 业务逻辑：
     * 1. 先删除该任务的所有旧关联（DELETE FROM okr_task_user WHERE task_id = ?）
     * 2. 批量插入新关联（INSERT INTO okr_task_user (user_id, task_id) VALUES ...）
     * <p>
     * 这样设计简单可靠：不需要 diff 新旧列表，直接全量替换。
     * 如果 userIds 为空，则等于取消所有指派。
     *
     * @param taskId  任务ID
     * @param userIds 要指派的用户ID列表（全量替换）
     */
    @Override
    public void assignUsers(Long taskId, List<Long> userIds) {
        log.info("开始指派任务: taskId = {}, userIds = {}", taskId, userIds);
        taskUserRepository.assignUsers(taskId, userIds);
    }

    /**
     * 查询「我的任务」
     * <p>
     * 业务逻辑：
     * 1. 通过 okr_task_user 关联表查出当前用户被指派的所有 taskId
     * 2. 批量查询这些 task 的详细信息（WHERE id IN (...)）
     * 3. 返回任务列表
     * <p>
     * 与 queryTaskListByKrId 的区别：
     * - queryTaskListByKrId：按 KR 查（需要权限校验）
     * - myTasks：按当前用户查（通过关联表，不需要额外权限校验，因为只查指派给自己的）
     *
     * @param currentUserId 当前登录用户ID
     * @return 被指派给当前用户的任务列表
     */
    @Override
    public List<OkrTaskVO> myTasks(Long currentUserId) {
        log.info("开始查询我的任务: currentUserId = {}", currentUserId);

        // 先查关联表获取 taskId 列表
        List<Long> taskIds = taskUserRepository.queryTaskIdsByUserId(currentUserId);

        // 批量查任务详情
        return repository.queryTasksByTaskIds(taskIds);
    }
}
