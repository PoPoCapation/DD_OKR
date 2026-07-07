package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 进度记录目标类型
 * <p>
 * 统一定义 okr_progress_record.target_type 的合法取值，避免裸字符串散落各处。
 * - OBJECTIVE：目标进度（由 KR 加权重算得到）
 * - KR：关键结果完成度
 * - TASK：任务进度（状态变更）
 */
@Getter
@AllArgsConstructor
public enum ProgressTargetType {

    OBJECTIVE("OBJECTIVE", "目标"),
    KR("KR", "关键结果"),
    TASK("TASK", "任务");

    private final String code;
    private final String desc;

    public String code() {
        return code;
    }
}
