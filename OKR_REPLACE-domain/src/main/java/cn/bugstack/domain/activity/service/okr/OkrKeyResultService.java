package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOkrKeyResultService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OkrKeyResultService implements IOkrKeyResultService {

    @Resource
    private IOkrKeyResultRepository repository;
    @Resource
    private IOkrObjectiveRepository objectiveRepository;
    @Resource
    private IUserService userService;

    @Override
    public void createKeyResult(OkrKeyResultVO vo) {
        log.info("开始创建KR: krName = {}, objectiveId = {}", vo.getKrName(), vo.getObjectiveId());
        if (!repository.createKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_CREATE_FAIL.getCode(), ResponseCode.OKR_KR_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateKeyResult(OkrKeyResultVO vo) {
        log.info("开始更新KR: id = {}", vo.getId());
        if (!repository.updateKeyResult(vo)) {
            throw new AppException(ResponseCode.OKR_KR_UPDATE_FAIL.getCode(), ResponseCode.OKR_KR_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteKeyResult(Long krId) {
        log.info("开始删除KR: id = {}", krId);
        if (!repository.deleteKeyResult(krId)) {
            throw new AppException(ResponseCode.OKR_KR_DELETE_FAIL.getCode(), ResponseCode.OKR_KR_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public List<OkrKeyResultVO> queryKeyResultListByObjectiveId(Long currentUserId, Long objectiveId) {
        log.info("开始查询KR列表: currentUserId = {}, objectiveId = {}", currentUserId, objectiveId);
        // 数据权限:校验当前用户对该 O 可见
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(objectiveId);
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        if (!visibleUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
        return repository.queryKeyResultListByObjectiveId(objectiveId);
    }
}
