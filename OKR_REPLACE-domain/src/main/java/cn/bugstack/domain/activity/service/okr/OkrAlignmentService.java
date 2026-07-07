package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveAlignmentRepository;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveAlignmentVO;
import cn.bugstack.domain.activity.service.IOkrAlignmentService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OKR 目标对齐（Alignment）领域服务
 * <p>
 * 职责：
 * 1. 创建对齐关系 —— 将一个目标对齐到上级目标（linkObjectiveToParent）
 * 2. 解除对齐关系 —— 删除对齐记录（unlinkAlignment）
 * 3. 查询对齐树 —— 出向（我对齐到谁）/ 入向（谁对齐到我）
 * 4. 防环校验 —— DFS 向上遍历，禁止 A→B→A 的循环对齐
 * <p>
 * 对齐的含义：
 * - 下级目标对齐到上级目标，表示「我的工作支撑上级的目标」
 * - 对齐是树形结构：一个 O 只能向上对齐到一个父 O（单出向），但可以被多个子 O 对齐（多入向）
 *
 * @see IOkrAlignmentService 接口定义
 */
@Slf4j
@Service
public class OkrAlignmentService implements IOkrAlignmentService {

    /** 对齐 Repository —— 封装 okr_objective_alignment 表的增删改查 */
    @Resource
    private IOkrObjectiveAlignmentRepository alignmentRepository;

    /**
     * 创建对齐关系（将 objectiveId 对齐到 parentObjectiveId）
     * <p>
     * 业务逻辑：
     * 1. 禁止对齐到自己（objectiveId == parentObjectiveId → 抛异常）
     * 2. 防环校验：从 parentObjectiveId 开始向上 DFS 遍历，
     *    如果遇到 objectiveId 说明会形成环（A→B→A），抛异常
     * 3. 创建对齐记录：alignmentType=upward（向上对齐）、status=active
     *
     * @param currentUserId      操作人ID（记录 createdBy）
     * @param objectiveId        要对齐的目标ID（子目标）
     * @param parentObjectiveId  对齐到的上级目标ID（父目标）
     * @throws AppException 对齐到自己 / 形成环 / 创建失败
     */
    @Override
    public void linkObjectiveToParent(Long currentUserId, Long objectiveId, Long parentObjectiveId) {
        log.info("开始对齐: objectiveId={}, parentObjectiveId={}", objectiveId, parentObjectiveId);

        // 禁止对齐到自己
        if (objectiveId.equals(parentObjectiveId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "不能对齐到自己");
        }

        // 防环校验：从 parent 向上遍历，如果遇到 objectiveId 则成环
        if (wouldCreateCycle(objectiveId, parentObjectiveId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "对齐会形成环");
        }

        // 创建对齐记录
        OkrObjectiveAlignmentVO vo = OkrObjectiveAlignmentVO.builder()
                .objectiveId(objectiveId)             // 子目标
                .alignedObjectiveId(parentObjectiveId) // 父目标
                .alignmentType("upward")               // 向上对齐
                .status("active")                      // 有效
                .createdBy(currentUserId)              // 操作人
                .isDeleted(0)
                .build();
        if (!alignmentRepository.createAlignment(vo)) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "创建对齐失败");
        }
    }

    /**
     * 防环校验：从 parentObjectiveId 开始向上 DFS，如果遇到 objectiveId 则会成环
     * <p>
     * 原理：
     * - 如果 A 要对齐到 B，那么从 B 向上遍历它的所有上级
     * - 如果遍历过程中遇到 A，说明 B 最终会指回 A，形成环
     * <p>
     * 安全限制：最多遍历 100 层（防止恶意构造超深链路导致栈溢出）
     *
     * @param objectiveId       要对齐的目标（子）
     * @param parentObjectiveId 对齐到的目标（父）
     * @return true=会成环，false=安全
     */
    private boolean wouldCreateCycle(Long objectiveId, Long parentObjectiveId) {
        Long current = parentObjectiveId;
        int maxDepth = 100; // 最大深度限制，防止恶意超深链路
        while (current != null && maxDepth-- > 0) {
            // 遇到自己 → 成环
            if (current.equals(objectiveId)) return true;

            // 查当前节点的出向对齐（它对齐到哪个上级）
            List<OkrObjectiveAlignmentVO> outbound = alignmentRepository.findOutbound(current);
            if (outbound == null || outbound.isEmpty()) break; // 没有上级 → 到顶了，安全

            // 继续向上
            current = outbound.get(0).getAlignedObjectiveId();
        }
        return false; // 遍历完没遇到自己 → 安全
    }

    /**
     * 解除对齐关系（逻辑删除）
     *
     * @param alignmentId 对齐记录ID
     * @throws AppException 解除失败
     */
    @Override
    public void unlinkAlignment(Long alignmentId) {
        log.info("解除对齐: alignmentId={}", alignmentId);
        if (!alignmentRepository.deleteAlignment(alignmentId)) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "解除对齐失败");
        }
    }

    /**
     * 查询出向对齐（我对齐到哪些上级目标）
     *
     * @param objectiveId 目标ID
     * @return 出向对齐列表
     */
    @Override
    public List<OkrObjectiveAlignmentVO> queryOutbound(Long objectiveId) {
        return alignmentRepository.findOutbound(objectiveId);
    }

    /**
     * 查询入向对齐（哪些下级目标对齐到我）
     *
     * @param objectiveId 目标ID
     * @return 入向对齐列表
     */
    @Override
    public List<OkrObjectiveAlignmentVO> queryInbound(Long objectiveId) {
        return alignmentRepository.findInbound(objectiveId);
    }
}
