package cn.bugstack.cases.okr.taskquery.node;

import cn.bugstack.cases.okr.taskquery.factory.TaskQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 节点1：校验 krId→KR→O 的 owner 在可见用户范围 */
@Slf4j
@Service("CheckVisibleTaskNode")
public class CheckVisibleTaskNode extends AbstractTaskQueryCaseSupport {
    @Resource(name = "QueryTaskNode")
    private QueryTaskNode queryTaskNode;

    @Override
    protected List<OkrTaskVO> doApply(Long currentUserId, TaskQueryCaseFactory.TaskQueryContext ctx) throws Exception {
        OkrKeyResultVO kr = keyResultRepository.queryKeyResultById(ctx.getKrId());
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
        return router(currentUserId, ctx);
    }

    @Override
    public StrategyHandler<Long, TaskQueryCaseFactory.TaskQueryContext, List<OkrTaskVO>> get(Long currentUserId, TaskQueryCaseFactory.TaskQueryContext ctx) throws Exception {
        return queryTaskNode;
    }
}
