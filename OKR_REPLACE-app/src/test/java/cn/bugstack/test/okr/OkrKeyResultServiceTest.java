package cn.bugstack.test.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.service.IOkrAuditService;
import cn.bugstack.domain.activity.service.IOKRObjectiveService;
import cn.bugstack.domain.activity.service.okr.OkrKeyResultService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ProgressTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OkrKeyResultService 单测：
 * 验证 KR 增删改后「写 KR 进度流水 + 操作日志 + 触发目标重算」是否正确。
 */
public class OkrKeyResultServiceTest {

    @Mock private IOkrKeyResultRepository repository;
    @Mock private IOkrObjectiveRepository objectiveRepository;
    @Mock private IUserService userService;
    @Mock private IOKRObjectiveService objectiveService;
    @Mock private IOkrAuditService auditService;

    private OkrKeyResultService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OkrKeyResultService();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "repository", repository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectiveRepository", objectiveRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "userService", userService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectiveService", objectiveService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "auditService", auditService);
    }

    @Test
    public void create_setsDefaults_writesAudit_triggersRecalc() {
        OkrKeyResultVO vo = OkrKeyResultVO.builder().krName("KR1").objectiveId(1L).build();
        when(repository.createKeyResult(vo)).thenReturn(true);

        service.createKeyResult(9L, vo);

        // 默认值
        assertEquals("todo", vo.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(vo.getCompletionRate()));
        assertEquals(Integer.valueOf(0), vo.getIsDeleted());
        // KR 流水（null -> 0）+ 操作日志（CREATE）
        verify(auditService).recordProgress(eq(ProgressTargetType.KR.code()), isNull(), isNull(),
                eq(BigDecimal.ZERO), eq("KR_CREATE"), eq(9L), eq("新建 KR"));
        verify(auditService).recordOperation(eq("OkrKeyResultService"), eq("KR"), isNull(), eq("CREATE"),
                eq(9L), isNull(), eq(vo));
        // 重算父目标
        verify(objectiveService).recalculateObjectiveProgress(eq(1L), eq(9L), eq("KR_CREATE"));
    }

    @Test
    public void update_writesOldToNewAudit_andRecalc() {
        OkrKeyResultVO before = OkrKeyResultVO.builder().id(5L).completionRate(new BigDecimal("20")).objectiveId(10L).build();
        OkrKeyResultVO after = OkrKeyResultVO.builder().id(5L).completionRate(new BigDecimal("80")).objectiveId(10L).build();
        OkrKeyResultVO vo = OkrKeyResultVO.builder().id(5L).completionRate(new BigDecimal("80")).build();
        when(repository.queryKeyResultById(5L)).thenReturn(before, after);
        when(repository.updateKeyResult(vo)).thenReturn(true);

        service.updateKeyResult(9L, vo);

        // KR 流水 old(20) -> new(80)，目标用 before 的 objectiveId(10)
        verify(auditService).recordProgress(eq(ProgressTargetType.KR.code()), eq(5L),
                eq(new BigDecimal("20")), eq(new BigDecimal("80")), eq("KR_UPDATE"), eq(9L), eq("更新 KR"));
        verify(auditService).recordOperation(eq("OkrKeyResultService"), eq("KR"), eq(5L), eq("UPDATE"),
                eq(9L), eq(before), eq(after));
        verify(objectiveService).recalculateObjectiveProgress(eq(10L), eq(9L), eq("KR_UPDATE"));
    }

    @Test
    public void delete_writesOldToNullAudit_andRecalc() {
        OkrKeyResultVO before = OkrKeyResultVO.builder().id(5L).completionRate(new BigDecimal("20")).objectiveId(10L).build();
        when(repository.queryKeyResultById(5L)).thenReturn(before);
        when(repository.deleteKeyResult(5L)).thenReturn(true);

        service.deleteKeyResult(9L, 5L);

        verify(auditService).recordProgress(eq(ProgressTargetType.KR.code()), eq(5L),
                eq(new BigDecimal("20")), isNull(), eq("KR_DELETE"), eq(9L), eq("删除 KR"));
        verify(auditService).recordOperation(eq("OkrKeyResultService"), eq("KR"), eq(5L), eq("DELETE"),
                eq(9L), eq(before), isNull());
        verify(objectiveService).recalculateObjectiveProgress(eq(10L), eq(9L), eq("KR_DELETE"));
    }

    @Test
    public void update_notFound_throws() {
        when(repository.queryKeyResultById(5L)).thenReturn(null);
        boolean threw = false;
        try {
            service.updateKeyResult(9L, OkrKeyResultVO.builder().id(5L).build());
        } catch (cn.bugstack.types.exception.AppException e) {
            threw = true;
        }
        assertTrue(threw, "KR 不存在应抛 AppException");
    }
}
