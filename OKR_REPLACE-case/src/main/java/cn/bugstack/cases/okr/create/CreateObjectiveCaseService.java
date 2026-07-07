package cn.bugstack.cases.okr.create;

import cn.bugstack.cases.okr.create.factory.CreateObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreateObjectiveCaseService implements ICreateObjectiveCase {

    @Resource
    private CreateObjectiveCaseFactory factory;

    @Override
    public Boolean createObjective(Long currentUserId, OkrObjectiveVO vo) {
        CreateObjectiveCaseFactory.ObjectiveContext context = new CreateObjectiveCaseFactory.ObjectiveContext();
        context.setObjective(vo);
        StrategyHandler<Long, CreateObjectiveCaseFactory.ObjectiveContext, Boolean> handler = factory.strategyHandler();
        try {
            return handler.apply(currentUserId, context);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
