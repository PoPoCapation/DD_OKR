package cn.bugstack.cases.okr.krquery.node;

import cn.bugstack.cases.okr.krquery.factory.KrQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** 节点1：校验当前用户对该 O 可见 */
@Slf4j
@Service("CheckVisibleKrNode")
public class CheckVisibleKrNode extends AbstractKrQueryCaseSupport {
    @Resource(name = "QueryKrNode")
    private QueryKrNode queryKrNode;

    @Override
    protected List<OkrKeyResultVO> doApply(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(ctx.getObjectiveId());
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
    public StrategyHandler<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> get(Long currentUserId, KrQueryCaseFactory.KrQueryContext ctx) throws Exception {
        return queryKrNode;
    }
}
