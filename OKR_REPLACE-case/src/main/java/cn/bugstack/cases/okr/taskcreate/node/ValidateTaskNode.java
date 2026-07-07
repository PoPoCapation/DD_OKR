package cn.bugstack.cases.okr.taskcreate.node;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 节点1：权限校验 —— 当前用户必须对任务关联 KR 的父目标具备「可编辑」权限才能创建任务。
 */
@Slf4j
@Service("ValidateTaskNode")
public class ValidateTaskNode extends AbstractTaskCreateCaseSupport {

    @Resource(name = "CreateTaskNode")
    private CreateTaskNode createTaskNode;

    @Override
    protected Boolean doApply(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        Long currentUserId = ctx.getCurrentUserId();
        Long krId = vo.getKrId();
        if (currentUserId == null || krId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "currentUserId/krId 不能为空");
        }
        OkrKeyResultVO kr = keyResultRepository.queryKeyResultById(krId);
        if (kr == null) {
            throw new AppException(ResponseCode.OKR_KR_FIND_FAIL.getCode(), ResponseCode.OKR_KR_FIND_FAIL.getInfo());
        }
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(kr.getObjectiveId());
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
    public StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> get(OkrTaskVO vo, TaskCreateCaseFactory.TaskCreateContext ctx) throws Exception {
        return createTaskNode;
    }
}
