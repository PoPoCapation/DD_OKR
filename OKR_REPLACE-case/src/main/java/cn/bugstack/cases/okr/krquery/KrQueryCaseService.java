package cn.bugstack.cases.okr.krquery;

import cn.bugstack.cases.okr.krquery.factory.KrQueryCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KrQueryCaseService implements IKrQueryCase {

    @Resource
    private KrQueryCaseFactory factory;

    @Override
    public List<OkrKeyResultVO> queryKeyResultList(Long currentUserId, Long objectiveId) {
        KrQueryCaseFactory.KrQueryContext ctx = new KrQueryCaseFactory.KrQueryContext();
        ctx.setObjectiveId(objectiveId);
        StrategyHandler<Long, KrQueryCaseFactory.KrQueryContext, List<OkrKeyResultVO>> handler = factory.strategyHandler();
        try {
            return handler.apply(currentUserId, ctx);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
