package cn.bugstack.cases.okr.krcreate.node;

import cn.bugstack.cases.okr.krcreate.factory.KrCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 节点2：创建 KR */
@Slf4j
@Service("CreateKrNode")
public class CreateKrNode extends AbstractKrCreateCaseSupport {
    @Resource(name = "KrCreateEndNode")
    private KrCreateEndNode endNode;

    @Override
    protected Boolean doApply(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        if (!keyResultRepository.createKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_CREATE_FAIL.getCode(), ResponseCode.OKR_KR_CREATE_FAIL.getInfo());
        }
        return router(vo, ctx);
    }

    @Override
    public StrategyHandler<OkrKeyResultVO, KrCreateCaseFactory.KrCreateContext, Boolean> get(OkrKeyResultVO vo, KrCreateCaseFactory.KrCreateContext ctx) throws Exception {
        return endNode;
    }
}
