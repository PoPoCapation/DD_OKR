package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOkrKeyResultService;
import cn.bugstack.domain.activity.service.IOKRObjectiveService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OKR 关键结果（Key Result）领域服务
 * <p>
 * 职责：
 * 1. KR 的 CRUD（创建/更新/删除/按目标查询）
 * 2. KR 变动后自动触发 {@link IOKRObjectiveService#recalculateObjectiveProgress} 重算父目标进度
 * 3. 数据权限 —— 查询 KR 列表时校验当前用户对父目标的可见性
 *
 * @see IOkrKeyResultService 接口定义
 */
@Slf4j
@Service
public class OkrKeyResultService implements IOkrKeyResultService {

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

    /**
     * 创建 KR
     * <p>
     * 业务逻辑：
     * 1. 调用 Repository 持久化 KR，失败抛 OKR_KR_CREATE_FAIL
     * 2. 自动触发 {@link IOKRObjectiveService#recalculateObjectiveProgress} 重算父目标进度
     *    （新增 KR 会改变权重总和，进度需要重新加权计算）
     *
     * @param vo KR 信息（krName + objectiveId 必填，weight/completionRate 可选）
     */
    @Override
    public void createKeyResult(OkrKeyResultVO vo) {
        log.info("开始创建KR: krName = {}, objectiveId = {}", vo.getKrName(), vo.getObjectiveId());
        if (!repository.createKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_CREATE_FAIL.getCode(), ResponseCode.OKR_KR_CREATE_FAIL.getInfo());
        }
        // KR 变动后重算父目标进度（加权平均）
        objectiveService.recalculateObjectiveProgress(vo.getObjectiveId());
    }

    /**
     * 更新 KR
     * <p>
     * 业务逻辑：
     * 1. 调用 Repository 更新 KR（动态 SQL，只更新非 null 字段），失败抛 OKR_KR_UPDATE_FAIL
     * 2. 自动触发重算父目标进度
     *    （前端「改进度」按钮调用此方法，更新 completionRate 后 O 的 progress 自动重算）
     *
     * @param vo KR 信息（id 必填，其余为要更新的字段）
     */
    @Override
    public void updateKeyResult(OkrKeyResultVO vo) {
        log.info("开始更新KR: id = {}", vo.getId());
        if (!repository.updateKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_UPDATE_FAIL.getCode(), ResponseCode.OKR_KR_UPDATE_FAIL.getInfo());
        }
        // 进度/权重变化后重算父目标
        objectiveService.recalculateObjectiveProgress(vo.getObjectiveId());
    }

    /**
     * 删除 KR（逻辑删除）
     * <p>
     * 业务逻辑：
     * 1. 先查 KR 获取其 objectiveId（删除后无法再查，必须先记下来）
     * 2. KR 不存在则抛 OKR_KR_FIND_FAIL
     * 3. 逻辑删除，失败抛 OKR_KR_DELETE_FAIL
     * 4. 用之前记录的 objectiveId 触发重算（删除 KR 后权重总和变了）
     *
     * @param krId 要删除的 KR ID
     */
    @Override
    public void deleteKeyResult(Long krId) {
        log.info("开始删除KR: id = {}", krId);

        // 先查出 KR，记下 objectiveId（删完后还需要它来重算进度）
        OkrKeyResultVO kr = repository.queryKeyResultById(krId);
        if (kr == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }
        if (!repository.deleteKeyResult(krId)) {
            throw new AppException(ResponseCode.OKR_KR_DELETE_FAIL.getCode(), ResponseCode.OKR_KR_DELETE_FAIL.getInfo());
        }
        // 删除 KR 后重算父目标（少了这个 KR 的权重贡献）
        objectiveService.recalculateObjectiveProgress(kr.getObjectiveId());
    }

    /**
     * 查询某目标下的所有 KR（带数据权限校验）
     * <p>
     * 业务逻辑：
     * 1. 查父目标，不存在抛 OKR_OBJECTIVE_FIND_FAIL
     * 2. 校验当前用户对该目标的可见性：
     *    用 {@link IUserService#queryVisibleUserIds}（自己 + 上级 + 下级）
     *    判断目标 ownerUserId 是否在可见范围内
     * 3. 不可见则抛 OKR_OBJECTIVE_NO_PERMISSION
     * 4. 可见则返回该目标下所有 KR
     *
     * @param currentUserId 当前登录用户ID
     * @param objectiveId   目标ID
     * @return 该目标下的 KR 列表
     * @throws AppException 目标不存在或无权查看
     */
    @Override
    public List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long currentUserId, Long objectiveId) {
        log.info("开始查询KR列表: currentUserId = {}, objectiveId = {}", currentUserId, objectiveId);

        // 查父目标
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(objectiveId);
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }

        // 数据权限：当前用户必须对该目标可见（能看目标才能看目标下的 KR）
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        if (!visibleUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }

        return repository.queryKeyResultListByObjectiveId(objectiveId);
    }
}
