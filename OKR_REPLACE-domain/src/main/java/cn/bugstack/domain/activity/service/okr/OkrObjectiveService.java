package cn.bugstack.domain.activity.service.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOKRObjectiveService;
import cn.bugstack.domain.user.model.entity.SystemUserVO;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * OKR Ŀ�� Service������Ȩ�ް��㱨��ϵ���Լ� + ֱ���ϼ� + ȫ���¼��ݹ飩
 */
@Slf4j
@Service
public class OkrObjectiveService implements IOKRObjectiveService {

    @Resource
    private IOkrObjectiveRepository objectiveRepository;
    @Resource
    private IUserService userService;

    @Override
    public void createObjective(Long currentUserId, OkrObjectiveVO vo) {
        log.info("��ʼ����Objective: name = {}, currentUserId = {}", vo.getObjectiveName(), currentUserId);
        SystemUserVO currentUser = userService.queryUserByUserId(currentUserId);
        vo.setOwnerUserId(currentUserId);
        vo.setDepartmentId(currentUser.getDepartmentId());
        if (vo.getStatus() == null) vo.setStatus("draft");
        if (vo.getProgress() == null) vo.setProgress(BigDecimal.ZERO);
        if (vo.getIsDeleted() == null) vo.setIsDeleted(0);
        vo.setCreatetime(new Date());
        vo.setUpdatetime(new Date());
        if (!objectiveRepository.createObjective(vo)) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_CREATE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_CREATE_FAIL.getInfo());
        }
    }

    @Override
    public void updateObjective(Long currentUserId, OkrObjectiveVO vo) {
        log.info("��ʼ����Objective: id = {}, currentUserId = {}", vo.getId(), currentUserId);
        checkOwnership(currentUserId, vo.getId());
        vo.setUpdatetime(new Date());
        if (!objectiveRepository.updateObjective(vo)) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_UPDATE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_UPDATE_FAIL.getInfo());
        }
    }

    @Override
    public void deleteObjective(Long currentUserId, Long objectiveId) {
        log.info("��ʼɾ��Objective: id = {}, currentUserId = {}", objectiveId, currentUserId);
        checkOwnership(currentUserId, objectiveId);
        if (!objectiveRepository.deleteObjective(objectiveId)) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_DELETE_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_DELETE_FAIL.getInfo());
        }
    }

    @Override
    public List<OkrObjectiveVO> queryObjectiveList(Long currentUserId) {
        log.info("��ʼ��ѯObjective�б�(�㱨��ϵ): currentUserId = {}", currentUserId);
        // �ɼ��û� = �Լ� + ֱ���ϼ� + ȫ���¼��ݹ�
        List<Long> visibleUserIds = userService.queryVisibleUserIds(currentUserId);
        return objectiveRepository.queryObjectiveListByUserIds(visibleUserIds);
    }

    /** У�鵱ǰ�û��Ƿ���Ȩ������Ŀ�꣨Ŀ�긺�����ڿɼ��û���Χ�ڣ� */
    private void checkOwnership(Long currentUserId, Long objectiveId) {
        OkrObjectiveVO objective = objectiveRepository.queryObjectiveById(objectiveId);
        if (objective == null) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getCode(), ResponseCode.OKR_OBJECTIVE_FIND_FAIL.getInfo());
        }
        List<Long> editableUserIds = userService.queryEditableUserIds(currentUserId);
        if (!editableUserIds.contains(objective.getOwnerUserId())) {
            throw new AppException(ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getCode(), ResponseCode.OKR_OBJECTIVE_NO_PERMISSION.getInfo());
        }
    }
}

