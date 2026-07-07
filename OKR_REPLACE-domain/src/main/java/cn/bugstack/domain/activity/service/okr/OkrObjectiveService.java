package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOKRObjectiveService;
import cn.bugstack.domain.activity.service.IOkrAuditService;
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
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * OKR 目标（Objective）领域服务
 * <p>
 * 职责：
 * 1. 目标的 CRUD（创建/更新/删除/查询列表）
 * 2. 数据权限控制 —— 基于「汇报关系」过滤可见目标（自己 + 上级 + 下级）
 * 3. 编辑权限控制 —— 只能编辑自己 + 下级的目标（不能编辑上级的）
 * 4. 进度自动汇总 —— KR 完成度变化时，按权重加权重算 O 的进度
 * <p>
 * 数据权限分两层：
 * - 查询列表用 {@link IUserService#queryVisibleUserIds}（自己 + 上级 + 下级递归）→ 能看
 * - 编辑校验用 {@link IUserService#queryEditableUserIds}（自己 + 下级递归，不含上级）→ 能改
 *
 * @see IOKRObjectiveService 接口定义
 */
@Slf4j
@Service
public class OkrObjectiveService implements IOKRObjectiveService {

    /** 目标 Repository —— 封装 okr_objective 表的增删改查 */
    @Resource
    private IOkrObjectiveRepository objectiveRepository;

    /** KR Repository —— 用于重算目标进度时查询其下所有 KR */
    @Resource
    private IOkrKeyResultRepository keyResultRepository;

    /** 用户服务 —— 用于查询当前用户信息（部门ID）、可见用户列表、可编辑用户列表 */
    @Resource
    private IUserService userService;

    /** 审计服务 —— 写进度流水与操作日志（与业务同事务） */
    @Resource
    private IOkrAuditService auditService;

    /** 资源类型常量：目标 */
    private static final String RESOURCE_OBJECTIVE = "OBJECTIVE";

    /**
     * 创建目标（Objective）
     * <p>
     * 业务逻辑：
     * 1. 查询当前用户信息，自动填充 ownerUserId（负责人=创建人）和 departmentId（所属部门）
     * 2. 设置默认值：status=draft（草稿）、progress=0、isDeleted=0
     * 3. 设置 createtime / updatetime
     * 4. 调用 Repository 持久化，失败抛 OKR_OBJECTIVE_CREATE_FAIL
     *
     * @param currentUserId 当前登录用户ID（从 JWT 拦截器 request 属性获取）
     * @param vo            目标信息（objectiveName 必填，其余自动填充或可选）
     */
    @Override
    @Transactional
    public void createObjective(Long currentUserId, OkrObjectiveVO vo) {
        log.info("开始创建Objective: name = {}, currentUserId = {}", vo.getObjectiveName(), currentUserId);

        // 查当前用户，取 departmentId（目标归属创建人的部门）
        SystemUserVO currentUser = userService.queryUserByUserId(currentUserId);
        vo.setOwnerUserId(currentUserId);
        vo.setDepartmentId(currentUser.getDepartmentId());

        // 设置默认值
        if (vo.getStatus() == null) vo.setStatus("draft");           // 默认草稿状态
        if (vo.getProgress() == null) vo.setProgress(BigDecimal.ZERO); // 默认进度 0
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);           // 默认未删除
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());

        // 持久化
        if (!objectiveRepository.createObjective(vo)) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_CREATE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_CREATE_FAIL.getInfo());
        }

        // 操作日志（after = 新建后的目标快照）
        auditService.recordOperation("OkrObjectiveService", RESOURCE_OBJECTIVE, vo.getId(), "CREATE", currentUserId, null, vo);
    }

    /**
     * 更新目标
     * <p>
     * 业务逻辑：
     * 1. 校验数据权限 —— 当前用户必须在该目标的「可编辑范围」内（自己或下级的目标）
     * 2. 更新 updatetime
     * 3. 调用 Repository 更新，失败抛 OKR_OBJECTIVE_UPDATE_FAIL
     *
     * @param currentUserId 当前登录用户ID
     * @param vo            要更新的目标信息（id 必填）
     * @throws AppException 无权操作时抛 OKR_OBJECTIVE_NO_PERMISSION
     */
    @Override
    @Transactional
    public void updateObjective(Long currentUserId, OkrObjectiveVO vo) {
        log.info("开始更新Objective: id = {}, currentUserId = {}", vo.getId(), currentUserId);
        checkOwnership(currentUserId, vo.getId());
        OkrObjectiveVO before = objectiveRepository.queryObjectiveById(vo.getId());
        vo.setUpdatetime(new Date());
        if (!objectiveRepository.updateObjective(vo)) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_UPDATE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_UPDATE_FAIL.getInfo());
        }
        auditService.recordOperation("OkrObjectiveService", RESOURCE_OBJECTIVE, vo.getId(), "UPDATE", currentUserId, before, vo);
    }

    /**
     * 删除目标（逻辑删除，is_deleted=1）
     * <p>
     * 业务逻辑：
     * 1. 校验数据权限 —— 同 updateObjective，只能删自己或下级的目标
     * 2. 调用 Repository 逻辑删除，失败抛 OKR_OBJECTIVE_DELETE_FAIL
     *
     * @param currentUserId 当前登录用户ID
     * @param objectiveId   要删除的目标ID
     * @throws AppException 无权操作时抛 OKR_OBJECTIVE_NO_PERMISSION
     */
    @Override
    @Transactional
    public void deleteObjective(Long currentUserId, Long objectiveId) {
        log.info("开始删除Objective: id = {}, currentUserId = {}", objectiveId, currentUserId);
        checkOwnership(currentUserId, objectiveId);
        OkrObjectiveVO before = objectiveRepository.queryObjectiveById(objectiveId);
        if (!objectiveRepository.deleteObjective(objectiveId)) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_DELETE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_DELETE_FAIL.getInfo());
        }
        // 对齐引用清理见 Phase 3（OkrAlignmentService.cleanReferences）
        auditService.recordOperation("OkrObjectiveService", RESOURCE_OBJECTIVE, objectiveId, "DELETE", currentUserId, before, null);
    }

    /**
     * 查询目标列表（按汇报关系数据权限过滤）
     * <p>
     * 业务逻辑：
     * 1. 调用 {@link IUserService#queryVisibleUserIds} 获取当前用户的可见用户ID列表
     *    可见范围 = 自己 + 直属上级 + 全部下级递归（CTE 递归 SQL）
     * 2. 按可见用户ID列表查询目标（WHERE owner_user_id IN (...)）
     * <p>
     * 效果：
     * - 下级能看上级的 OKR（可见但不一定能改）
     * - 上级能看所有下级的 OKR
     * - 自己能看自己的 OKR
     *
     * @param currentUserId 当前登录用户ID
     * @return 可见的目标列表
     */
    @Override
    public List<OkrObjectiveVO> queryObjectiveList(Long currentUserId) {
        log.info("开始查询Objective列表(汇报关系): currentUserId = {}", currentUserId);
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        return objectiveRepository.queryObjectiveListByUserIds(visibleUserIds);
    }

    /**
     * 重算目标进度（按 KR 加权汇总）
     * <p>
     * 计算公式：objective.progress = Σ(KR.completionRate × KR.weight) / Σ(KR.weight)
     * <p>
     * 触发时机：KR 的 create / update / delete 操作后自动调用（见 {@link OkrKeyResultService}）
     * <p>
     * 特殊处理：
     * - 目标不存在 → 静默返回（防 NPE）
     * - 无 KR → 进度设为 0
     * - KR 权重全为 0 → 进度设为 0（避免除零）
     * - 保留 2 位小数，四舍五入
     *
     * @param objectiveId 目标ID
     */
    @Override
    @Transactional
    public void recalculateObjectiveProgress(Long objectiveId, Long operatorId, String sourceType) {
        log.info("开始重算目标进度: objectiveId = {}, operatorId = {}, sourceType = {}", objectiveId, operatorId, sourceType);

        List<OkrKeyResultVO> krs = keyResultRepository.queryKeyResultListByObjectiveId(objectiveId);
        OkrObjectiveVO vo = objectiveRepository.queryObjectiveById(objectiveId);
        if (vo == null) return; // 目标不存在，静默返回

        BigDecimal oldProgress = vo.getProgress() != null ? vo.getProgress() : BigDecimal.ZERO;

        // 无 KR → 进度归零
        if (krs == null || krs.isEmpty()) {
            vo.setProgress(BigDecimal.ZERO);
            vo.setUpdatetime(new Date());
            objectiveRepository.updateObjective(vo);
            auditService.recordProgress(ProgressTargetType.OBJECTIVE.code(), objectiveId, oldProgress, BigDecimal.ZERO, sourceType, operatorId, "无 KR，进度归零");
            return;
        }

        // 加权计算：Σ(完成度 × 权重) / Σ(权重)
        BigDecimal totalWeight = BigDecimal.ZERO;   // 权重总和
        BigDecimal weightedSum = BigDecimal.ZERO;    // 加权完成度总和
        for (OkrKeyResultVO kr : krs) {
            BigDecimal weight = kr.getWeight() != null ? kr.getWeight() : BigDecimal.ZERO;
            BigDecimal rate = kr.getCompletionRate() != null ? kr.getCompletionRate() : BigDecimal.ZERO;
            totalWeight = totalWeight.add(weight);
            weightedSum = weightedSum.add(rate.multiply(weight));
        }

        // 避免除零：权重总和 > 0 时计算加权平均，否则进度为 0
        BigDecimal progress = totalWeight.compareTo(BigDecimal.ZERO) > 0
            ? weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP)  // 保留2位小数，四舍五入
            : BigDecimal.ZERO;

        vo.setProgress(progress);
        vo.setUpdatetime(new Date());
        objectiveRepository.updateObjective(vo);

        // 写 OBJECTIVE 维度进度流水（old→new），便于追溯目标进度变化轨迹
        auditService.recordProgress(ProgressTargetType.OBJECTIVE.code(), objectiveId, oldProgress, progress, sourceType, operatorId, "KR 加权重算");
    }

    /**
     * 校验当前用户是否有权编辑/删除该目标
     * <p>
     * 权限规则（可编辑范围）：
     * - 自己的目标 → 可以
     * - 下级的目标 → 可以（上级管下级）
     * - 上级的目标 → 不可以（下级不能改上级的）
     * <p>
     * 实现：
     * 1. 查询目标，不存在则抛 OKR_OBJECTIVE_FIND_FAIL
     * 2. 调用 {@link IUserService#queryEditableUserIds} 获取可编辑用户ID列表
     *    可编辑范围 = 自己 + 全部下级递归（CTE 递归 SQL，不含直属上级）
     * 3. 判断目标的 ownerUserId 是否在可编辑范围内
     *
     * @param currentUserId 当前登录用户ID
     * @param objectiveId   目标ID
     * @throws AppException 目标不存在 或 无权操作
     */
    private void checkOwnership(Long currentUserId, Long objectiveId) {
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(objectiveId);
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> editableUserIds = userService.queryEditableUserIds(currentUserId);
        if (!editableUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
    }
}
