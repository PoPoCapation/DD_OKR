package cn.bugstack.cases.okr.taskcreate;

import cn.bugstack.cases.okr.taskcreate.factory.TaskCreateCaseFactory;
import cn.bugstack.domain.activity.model.entity.OkrTaskVO;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskCreateCaseService implements ITaskCreateCase {

    @Resource
    private TaskCreateCaseFactory factory;

    @Override
    public Boolean createTask(OkrTaskVO vo) {
        StrategyHandler<OkrTaskVO, TaskCreateCaseFactory.TaskCreateContext, Boolean> handler = factory.strategyHandler();
        try {
            return handler.apply(vo, new TaskCreateCaseFactory.TaskCreateContext());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
