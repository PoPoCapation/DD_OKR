package cn.bugstack.cases.okr.node;

import cn.bugstack.cases.okr.factory.CreateObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 节点1：查当前用户，设 owner/部门/默认值
 */
@Slf4j
@Service("SetOwnerDeptNode")
public class SetOwnerDeptNode extends AbstractCreateObjectiveCaseSupport {

    @Resource(name = "CreateObjectiveNode")
    private CreateObjectiveNode createObjectiveNode;

    @Override
    protected Boolean doApply(Long currentUserId, CreateObjectiveCaseFactory.ObjectiveContext context) throws Exception {
        SystemUserVO user = userService.queryUserByUserId(currentUserId);
        OkrObjectiveVO vo = context.getObjective();
        vo.setOwnerUserId(currentUserId);
        vo.setDepartmentId(user.getDepartmentId());
        if (vo.getStatus() == null) vo.setStatus("draft");
        if (vo.getProgress() == null) vo.setProgress(BigDecimal.ZERO);
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());
        return router(currentUserId, context);
    }

    @Override
    public StrategyHandler<Long, CreateObjectiveCaseFactory.ObjectiveContext, Boolean> get(Long currentUserId, CreateObjectiveCaseFactory.ObjectiveContext context) throws Exception {
        return createObjectiveNode;
    }
}
