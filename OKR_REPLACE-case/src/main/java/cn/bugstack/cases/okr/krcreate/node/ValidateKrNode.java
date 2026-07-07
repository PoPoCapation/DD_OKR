package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点1：权限校验 —— 当前用户必须对父目标具备「可编辑」权限（自己或下级的目标）才能在其下创建 KR。
 */
@Slf4j
@Service("ValidateKrNode")
public class ValidateKrNode extends AbstractKrCreateCaseSupport {

    @Resource(name = "CreateKrNode")
    private CreateKrNode createKrNode;

    @Override
    protected Boolean doApply(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        Long currentUserId = ctx.getCurrentUserId();
        Long objectiveId = vo.getObjectiveId();
        if (currentUserId == null || objectiveId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "currentUserId/objectiveId 不能为空");
        }
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(objectiveId);
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> editableUserIds = userService.queryEditableUserIds(currentUserId);
        if (!editableUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> get(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return createKrNode;
    }
}