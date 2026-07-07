package cn.bugstack.cases.okr.krcreate;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KrCreateCaseService implements IKrCreateCase {

    @Resource
    private KrCreateCaseFactory factory;

    @Override
    public Boolean createKeyResult(Long currentUserId, OkrKeyResultVO vo) {
        KrCreateCaseFactory.KrCreateContext ctx = KrCreateCaseFactory.KrCreateContext.builder()
                .currentUserId(currentUserId)
                .build();
        StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> handler = factory.strategyHandler();
        try {
            return handler.apply(vo, ctx);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
