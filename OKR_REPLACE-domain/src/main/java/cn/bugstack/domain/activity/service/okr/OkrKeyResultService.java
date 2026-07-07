package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOkrAuditService;
import cn.bugstack.domain.activity.service.IOkrKeyResultService;
import cn.bugstack.domain.activity.service.IOKRObjectiveService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.common.PageResult;
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
 * OKR 关键结果（Key Result）领域服务
 * <p>
 * 职责：
 * 1. KR 的 CRUD（创建/更新/删除/按目标查询/分页查询）
 * 2. KR 变动后自动触发 {@link IOKRObjectiveService#recalculateObjectiveProgress} 重算父目标进度
 * 3. KR 增删改写入进度流水（KR 维度）与操作日志，并与业务同事务
 * 4. 数据权限 —— 查询 KR 列表时校验当前用户对父目标的可见性
 *
 * @see IOkrKeyResultService 接口定义
 */
@Slf4j
@Service
public class OkrKeyResultService implements IOkrKeyResultService {

    /** 进度流水来源常量 */
    private static final String SOURCE_KR_CREATE = "KR_CREATE";
    private static final String SOURCE_KR_UPDATE = "KR_UPDATE";
    private static final String SOURCE_KR_DELETE = "KR_DELETE";
    private static final String RESOURCE_KR = "KR";

    /** KR Repository —— 封装 okr_key_result 表的增删改查 */
    @Resource
    private IOkrKeyResultRepository repository;

    /** 目标 Repository —— 查询父目标信息（用于数据权限校验） */
    @Resource
    private IOkrObjectiveRepository objectiveRepository;

    /** 用户服务 —— 获取可见用户列表（数据权限校验用） */
    @Resource
    private IUserService userService;

    /** 目标服务 —— KR 变动后调用 recalculateObjectiveProgress 重算进度 */
    @Resource
    private IOKRObjectiveService objectiveService;

    /** 审计服务 —— 写 KR 进度流水与操作日志 */
    @Resource
    private IOkrAuditService auditService;

    /**
     * 创建 KR
     * <p>
     * 业务逻辑：
     * 1. 设置默认值（status=todo、completionRate=0、isDeleted=0、时间）
     * 2. 持久化，失败抛 OKR_KR_CREATE_FAIL
     * 3. 写 KR 进度流水（null → completionRate）与操作日志（CREATE）
     * 4. 自动触发重算父目标进度（加权平均，并写 OBJECTIVE 流水）
     *
     * @param currentUserId 操作人（审计用）
     * @param vo            KR 信息（krName + objectiveId 必填，weight/completionRate 可选）
     */
    @Override
    @Transactional
    public void createKeyResult(Long currentUserId, OkrKeyResultVO vo) {
        log.info("开始创建KR: krName = {}, objectiveId = {}, currentUserId = {}", vo.getKrName(), vo.getObjectiveId(), currentUserId);

        // 默认值
        if (vo.getStatus() == null) vo.setStatus("todo");
        if (vo.getCompletionRate() == null) vo.setCompletionRate(BigDecimal.ZERO);
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());

        if (!repository.createKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_CREATE_FAIL.getCode(), ResponseCode.OKR_KR_CREATE_FAIL.getInfo());
        }

        // KR 进度流水（新建：null → completionRate）+ 操作日志
        auditService.recordProgress(ProgressTargetType.KR.code(), vo.getId(), null, vo.getCompletionRate(), SOURCE_KR_CREATE, currentUserId, "新建 KR");
        auditService.recordOperation("OkrKeyResultService", RESOURCE_KR, vo.getId(), "CREATE", currentUserId, null, vo);

        // KR 变动后重算父目标进度（加权平均，内部写 OBJECTIVE 流水）
        objectiveService.recalculateObjectiveProgress(vo.getObjectiveId(), currentUserId, SOURCE_KR_CREATE);
    }

    /**
     * 更新 KR
     * <p>
     * 业务逻辑：
     * 1. 查旧 KR（记 objectiveId / 旧完成度，用于重算与流水）
     * 2. 动态更新（只更新非 null 字段），失败抛 OKR_KR_UPDATE_FAIL
     * 3. 写 KR 进度流水（old → new）与操作日志（UPDATE，前后快照）
     * 4. 重算父目标进度
     *
     * @param currentUserId 操作人
     * @param vo            KR 信息（id 必填，其余为要更新的字段）
     */
    @Override
    @Transactional
    public void updateKeyResult(Long currentUserId, OkrKeyResultVO vo) {
        log.info("开始更新KR: id = {}, currentUserId = {}", vo.getId(), currentUserId);

        OkrKeyResultVO before = repository.queryKeyResultById(vo.getId());
        if (before == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }
        vo.setUpdatetime(new Date());
        if (!repository.updateKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_UPDATE_FAIL.getCode(), ResponseCode.OKR_KR_UPDATE_FAIL.getInfo());
        }

        // 计算新完成度（未传则保持旧值）；重算父目标用旧 KR 的 objectiveId（更新通常不改归属）
        BigDecimal oldRate = before.getCompletionRate() != null ? before.getCompletionRate() : BigDecimal.ZERO;
        BigDecimal newRate = vo.getCompletionRate() != null ? vo.getCompletionRate() : oldRate;
        Long objectiveId = before.getObjectiveId();

        // KR 进度流水（old → new）+ 操作日志（before / after 快照）
        auditService.recordProgress(ProgressTargetType.KR.code(), vo.getId(), oldRate, newRate, SOURCE_KR_UPDATE, currentUserId, "更新 KR");
        OkrKeyResultVO after = repository.queryKeyResultById(vo.getId());
        auditService.recordOperation("OkrKeyResultService", RESOURCE_KR, vo.getId(), "UPDATE", currentUserId, before, after);

        objectiveService.recalculateObjectiveProgress(objectiveId, currentUserId, SOURCE_KR_UPDATE);
    }

    /**
     * 删除 KR（逻辑删除）
     * <p>
     * 业务逻辑：
     * 1. 查旧 KR，记下 objectiveId / 旧完成度（删除后无法再查）
     * 2. 逻辑删除，失败抛 OKR_KR_DELETE_FAIL
     * 3. 写 KR 进度流水（old → null）与操作日志（DELETE）
     * 4. 用记录的 objectiveId 触发重算
     *
     * @param currentUserId 操作人
     * @param krId          要删除的 KR ID
     */
    @Override
    @Transactional
    public void deleteKeyResult(Long currentUserId, Long krId) {
        log.info("开始删除KR: id = {}, currentUserId = {}", krId, currentUserId);

        OkrKeyResultVO before = repository.queryKeyResultById(krId);
        if (before == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }
        if (!repository.deleteKeyResult(krId)) {
            throw new AppException(ResponseCode.OKR_KR_DELETE_FAIL.getCode(), ResponseCode.OKR_KR_DELETE_FAIL.getInfo());
        }

        BigDecimal oldRate = before.getCompletionRate() != null ? before.getCompletionRate() : BigDecimal.ZERO;
        auditService.recordProgress(ProgressTargetType.KR.code(), krId, oldRate, null, SOURCE_KR_DELETE, currentUserId, "删除 KR");
        auditService.recordOperation("OkrKeyResultService", RESOURCE_KR, krId, "DELETE", currentUserId, before, null);

        objectiveService.recalculateObjectiveProgress(before.getObjectiveId(), currentUserId, SOURCE_KR_DELETE);
    }

    /**
     * 查询某目标下的所有 KR（带数据权限校验）
     *
     * @param currentUserId 当前登录用户ID
     * @param objectiveId   目标ID
     * @return 该目标下的 KR 列表
     * @throws AppException 目标不存在或无权查看
     */
    @Override
    public List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long currentUserId, Long objectiveId) {
        log.info("开始查询KR列表: currentUserId = {}, objectiveId = {}", currentUserId, objectiveId);
        checkVisible(currentUserId, objectiveId);
        return repository.queryKeyResultListByObjectiveId(objectiveId);
    }

    /**
     * 分页查询某目标下的 KR（带数据权限校验）。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     */
    @Override
    public PageResult<OkrKeyResultVO> queryKeyResultPage(Long currentUserId, Long objectiveId, Integer page, Integer size) {
        log.info("开始分页查询KR: currentUserId = {}, objectiveId = {}, page = {}, size = {}", currentUserId, objectiveId, page, size);
        checkVisible(currentUserId, objectiveId);

        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = repository.countByObjectiveId(objectiveId);
        if (total <= 0) {
            return PageResult.empty(p, s);
        }
        List<OkrKeyResultVO> records = repository.queryKeyResultPage(objectiveId, (p - 1) * s, s);
        return PageResult.<OkrKeyResultVO>builder().records(records).total(total).page(p).size(s).build();
    }

    /**
     * 校验当前用户对父目标的可见性，不可见则抛无权异常。
     */
    private void checkVisible(Long currentUserId, Long objectiveId) {
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(objectiveId);
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        if (!visibleUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
    }
}
