package cn.bugstack.test.okr;

import cn.bugstack.domain.activity.adapter.repository.IOkrKeyResultRepository;
import cn.bugstack.domain.activity.adapter.repository.IOkrObjectiveRepository;
import cn.bugstack.domain.activity.model.entity.OkrKeyResultVO;
import cn.bugstack.domain.activity.model.entity.OkrObjectiveVO;
import cn.bugstack.domain.activity.service.IOkrAuditService;
import cn.bugstack.domain.activity.service.okr.OkrObjectiveService;
import cn.bugstack.domain.user.service.IUserService;
import cn.bugstack.types.enums.ProgressTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OkrObjectiveService.recalculateObjectiveProgress 单测：
 * 覆盖「无 KR 归零 / 加权平均 / 权重全 0 归零」并校验 OBJECTIVE 维度流水写入。
 */
public class OkrObjectiveServiceTest {

    @Mock private IOkrObjectiveRepository objectiveRepository;
    @Mock private IOkrKeyResultRepository keyResultRepository;
    @Mock private IUserService userService;
    @Mock private IOkrAuditService auditService;

    private OkrObjectiveService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OkrObjectiveService();
        // 注入 mock 依赖（字段注入）
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectiveRepository", objectiveRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "keyResultRepository", keyResultRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "userService", userService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "auditService", auditService);
    }

    private OkrObjectiveVO objective(Long id, BigDecimal progress) {
        return OkrObjectiveVO.builder().id(id).progress(progress).build();
    }

    private OkrKeyResultVO kr(BigDecimal rate, BigDecimal weight) {
        return OkrKeyResultVO.builder().completionRate(rate).weight(weight).build();
    }

    @Test
    public void recalc_noKr_progressZeroAndAudit() {
        when(keyResultRepository.queryKeyResultListByObjectiveId(1L)).thenReturn(Collections.emptyList());
        when(objectiveRepository.queryObjectiveById(1L)).thenReturn(objective(1L, new BigDecimal("50")));

        service.recalculateObjectiveProgress(1L, 9L, "KR_DELETE");

        ArgumentCaptor<OkrObjectiveVO> cap = ArgumentCaptor.forClass(OkrObjectiveVO.class);
        verify(objectiveRepository).updateObjective(cap.capture());
        assertEquals(0, BigDecimal.ZERO.compareTo(cap.getValue().getProgress()));
        verify(auditService).recordProgress(eq(ProgressTargetType.OBJECTIVE.code()), eq(1L),
                eq(new BigDecimal("50")), eq(BigDecimal.ZERO), eq("KR_DELETE"), eq(9L), anyString());
    }

    @Test
    public void recalc_weightedAverage() {
        when(keyResultRepository.queryKeyResultListByObjectiveId(1L)).thenReturn(
                Arrays.asList(kr(new BigDecimal("50"), new BigDecimal("2")), kr(new BigDecimal("100"), new BigDecimal("3"))));
        when(objectiveRepository.queryObjectiveById(1L)).thenReturn(objective(1L, BigDecimal.ZERO));

        service.recalculateObjectiveProgress(1L, 9L, "KR_UPDATE");

        ArgumentCaptor<OkrObjectiveVO> cap = ArgumentCaptor.forClass(OkrObjectiveVO.class);
        verify(objectiveRepository).updateObjective(cap.capture());
        // (50*2 + 100*3) / (2+3) = 400/5 = 80.00
        assertEquals(0, new BigDecimal("80.00").compareTo(cap.getValue().getProgress()));
        verify(auditService).recordProgress(eq(ProgressTargetType.OBJECTIVE.code()), eq(1L),
                eq(BigDecimal.ZERO), eq(new BigDecimal("80.00")), eq("KR_UPDATE"), eq(9L), anyString());
    }

    @Test
    public void recalc_allZeroWeight_progressZero() {
        when(keyResultRepository.queryKeyResultListByObjectiveId(1L)).thenReturn(
                Arrays.asList(kr(new BigDecimal("50"), BigDecimal.ZERO), kr(new BigDecimal("80"), BigDecimal.ZERO)));
        when(objectiveRepository.queryObjectiveById(1L)).thenReturn(objective(1L, new BigDecimal("30")));

        service.recalculateObjectiveProgress(1L, 9L, "KR_UPDATE");

        ArgumentCaptor<OkrObjectiveVO> cap = ArgumentCaptor.forClass(OkrObjectiveVO.class);
        verify(objectiveRepository).updateObjective(cap.capture());
        assertEquals(0, BigDecimal.ZERO.compareTo(cap.getValue().getProgress()));
        verify(auditService, times(1)).recordProgress(anyString(), anyLong(), any(), any(), anyString(), anyLong(), anyString());
    }

    @Test
    public void recalc_objectiveNotFound_noop() {
        when(objectiveRepository.queryObjectiveById(1L)).thenReturn(null);
        service.recalculateObjectiveProgress(1L, 9L, "KR_CREATE");
        verify(objectiveRepository, org.mockito.Mockito.never()).updateObjective(any());
        verify(auditService, org.mockito.Mockito.never()).recordProgress(anyString(), anyLong(), any(), any(), anyString(), anyLong(), anyString());
    }
}
