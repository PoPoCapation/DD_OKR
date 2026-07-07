package cn.bugstack.cases.okr.query;

import cn.bugstack.cases.okr.query.factory.QueryObjectiveCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class QueryObjectiveCaseService implements IQueryObjectiveCase {

    @Resource
    private QueryObjectiveCaseFactory factory;

    @Override
    public List<OkrObjectiveVO> queryObjectiveList(Long currentUserId) {
        QueryObjectiveCaseFactory.QueryContext context = new QueryObjectiveCaseFactory.QueryContext();
        StrategyHandler<Long, QueryObjectiveCaseFactory.QueryContext, List<OkrObjectiveVO>> handler = factory.strategyHandler();
        try {
            return handler.apply(currentUserId, context);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
