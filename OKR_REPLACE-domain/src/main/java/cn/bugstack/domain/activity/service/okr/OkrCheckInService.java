package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrCheckInRepository;
import cn.bugstack.domain.activity.model.entity.OkrCheckInItemVO;
import cn.bugstack.domain.activity.model.entity.OkrCheckInVO;
import cn.bugstack.domain.activity.service.IOkrCheckInService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OKR 复盘（Check-in）领域服务
 * <p>
 * 职责：
 * 1. 创建复盘 —— 包含主表（总结/风险/阻塞/计划/信心指数）+ 明细表（每个 KR 的进度变动）
 * 2. 查询复盘列表 —— 按目标ID查该目标的所有历史复盘
 * 3. 查询复盘明细 —— 查某次复盘下每个 KR 的 old→new 完成度变化
 * <p>
 * 复盘流程：
 * - 定期（如每周/每两周）对目标进行复盘
 * - 记录信心指数（1-5）、进展总结、风险、阻塞、下一步计划
 * - 同时记录每个 KR 的完成度变化（old_completion_rate → new_completion_rate）
 * - 复盘明细可用于追溯 KR 进度的历史变化轨迹
 *
 * @see IOkrCheckInService 接口定义
 */
@Slf4j
@Service
public class OkrCheckInService implements IOkrCheckInService {

    /** 复盘 Repository —— 封装 okr_check_in + okr_check_in_item 两张表 */
    @Resource
    private IOkrCheckInRepository repository;

    /**
     * 创建复盘（带明细）
     * <p>
     * 使用 @Transactional 保证原子性：主表 + 明细表一起提交，任一失败全部回滚。
     * <p>
     * 业务逻辑：
     * 1. 构建复盘主表 VO（objectiveId / confidence / summary / risk / blocker / nextPlan）
     * 2. 调用 Repository 插入主表，获取 checkInId（useGeneratedKeys 回填）
     * 3. 遍历明细列表，设置 checkInId 后逐条插入 okr_check_in_item
     * <p>
     * 明细（OkrCheckInItemVO）字段说明：
     * - krId：关联的 KR ID
     * - oldCompletionRate：复盘前 KR 的完成度
     * - newCompletionRate：复盘后 KR 的完成度（可能更新了进度）
     * - progressDelta：变化量（new - old）
     * - remark：该 KR 的进展说明
     *
     * @param currentUserId 当前登录用户ID（复盘提交人）
     * @param objectiveId   复盘的目标ID
     * @param confidence    信心指数（1-5，5=很有信心）
     * @param summary       进展总结
     * @param risk          风险说明
     * @param blocker       阻塞说明
     * @param nextPlan      下一步计划
     * @param items         KR 明细列表（每个 KR 的进度变动）
     * @return 复盘记录ID
     */
    @Override
    @Transactional // 主表+明细原子提交，失败回滚
    public Long createCheckIn(Long currentUserId, Long objectiveId, Integer confidence,
                              String summary, String risk, String blocker, String nextPlan,
                              List<OkrCheckInItemVO> items) {
        log.info("开始创建复盘: objectiveId={}, userId={}", objectiveId, currentUserId);

        // 1. 插入复盘主表
        OkrCheckInVO vo = OkrCheckInVO.builder()
                .objectiveId(objectiveId)
                .checkInUserId(currentUserId)
                .confidenceLevel(confidence)
                .summary(summary)
                .risk(risk)
                .blocker(blocker)
                .nextPlan(nextPlan)
                .build();
        Long checkInId = repository.createCheckIn(vo);

        // 2. 逐条插入 KR 明细（记录每个 KR 的 old→new 完成度）
        if (items != null) {
            for (OkrCheckInItemVO item : items) {
                item.setCheckInId(checkInId); // 设置关联的主表ID
                repository.insertItem(item);
            }
        }

        return checkInId;
    }

    /**
     * 查询某目标的所有复盘记录
     *
     * @param objectiveId 目标ID
     * @return 复盘列表（按提交时间倒序）
     */
    @Override
    public List<OkrCheckInVO> queryByObjectiveId(Long objectiveId) {
        return repository.queryByObjectiveId(objectiveId);
    }

    /**
     * 查询某次复盘的 KR 明细
     *
     * @param checkInId 复盘记录ID
     * @return KR 明细列表（每个 KR 的 old→new 完成度变化）
     */
    @Override
    public List<OkrCheckInItemVO> queryItems(Long checkInId) {
        return repository.queryItemsByCheckInId(checkInId);
    }
}
