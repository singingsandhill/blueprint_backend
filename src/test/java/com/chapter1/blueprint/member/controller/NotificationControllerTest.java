package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.service.MemberService;
import com.chapter1.blueprint.member.service.NotificationService;
import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import com.chapter1.blueprint.policy.service.PolicyRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private MemberService memberService;

    @Mock
    private PolicyRecommendationService policyRecommendationService;

    @Mock
    private PolicyListRepository policyListRepository;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateNotificationStatus_EnableNotifications() {
        // 알림을 ON으로 설정하는 테스트
        Long mockUid = 123L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);

        Map<String, Object> request = new HashMap<>();
        request.put("notificationEnabled", true);

        ResponseEntity<String> response = notificationController.updateNotificationStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notification status updated successfully.", response.getBody());
        verify(notificationService, times(1)).updateNotificationStatus(mockUid, true);
    }

    @Test
    void testUpdateNotificationStatus_DisableNotifications() {
        // 알림을 OFF로 설정하는 테스트
        Long mockUid = 123L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);

        Map<String, Object> request = new HashMap<>();
        request.put("notificationEnabled", false);

        ResponseEntity<String> response = notificationController.updateNotificationStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notification status updated successfully.", response.getBody());
        verify(notificationService, times(1)).updateNotificationStatus(mockUid, false);
    }

    @Test
    void testUpdateNotificationSettingsForPolicy() {
        // 특정 정책에 대해 알림을 설정하는 테스트
        Long mockUid = 123L;
        Long policyIdx = 1L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);

        Map<String, Object> request = new HashMap<>();
        request.put("notificationEnabled", true);
        request.put("applyEndDate", new Date());

        ResponseEntity<String> response = notificationController.updateNotificationSettings(policyIdx, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notification settings updated successfully.", response.getBody());
        verify(notificationService, times(1)).saveOrUpdateNotification(mockUid, policyIdx, true, (Date) request.get("applyEndDate"));
    }

    @Test
    void testDeleteNotificationSettings() {
        // 특정 정책에 대한 알림 설정을 삭제하는 테스트
        Long mockUid = 123L;
        Long policyIdx = 1L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);

        ResponseEntity<String> response = notificationController.deleteNotificationSettings(policyIdx);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notification settings deleted successfully.", response.getBody());
        verify(notificationService, times(1)).deleteNotification(mockUid, policyIdx);
    }

    @Test
    void testGetMemberDefinedNotifications() {
        // 사용자가 직접 설정한 알림 목록을 가져오는 테스트
        Long mockUid = 123L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);
        List<PolicyAlarm> mockNotifications = List.of(new PolicyAlarm());

        when(notificationService.getMemberNotifications(mockUid)).thenReturn(mockNotifications);

        ResponseEntity<?> response = notificationController.getMemberDefinedNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertEquals(mockNotifications, successResponse.getResponse().getData());
    }

    @Test
    void testGetRecommendedNotifications() {
        // 시스템에서 추천하는 알림 목록을 가져오는 테스트
        Long mockUid = 123L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);
        List<PolicyAlarm> mockRecommendedNotifications = List.of(new PolicyAlarm());

        when(notificationService.getRecommendedNotifications(mockUid)).thenReturn(mockRecommendedNotifications);

        ResponseEntity<?> response = notificationController.getRecommendedNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertEquals(mockRecommendedNotifications, successResponse.getResponse().getData());
    }

    @Test
    void testGetNotificationDashboard() {
        Long mockUid = 123L;
        when(memberService.getAuthenticatedUid()).thenReturn(mockUid);

        PolicyAlarm memberAlarm = new PolicyAlarm();
        memberAlarm.setPolicyIdx(1L);
        PolicyAlarm recommendedAlarm = new PolicyAlarm();
        recommendedAlarm.setPolicyIdx(2L);

        List<PolicyAlarm> mockMemberNotifications = List.of(memberAlarm);
        List<PolicyAlarm> mockRecommendedNotifications = List.of(recommendedAlarm);
        List<PolicyDetailFilter> mockRecommendedPolicies = List.of(new PolicyDetailFilter());

        PolicyList mockPolicy = new PolicyList();
        mockPolicy.setName("Test Policy");
        mockPolicy.setApplyEndDate(new Date());

        when(notificationService.getMemberNotifications(mockUid)).thenReturn(mockMemberNotifications);
        when(notificationService.getRecommendedNotifications(mockUid)).thenReturn(mockRecommendedNotifications);
        when(policyRecommendationService.getRecommendedPolicies(mockUid)).thenReturn(mockRecommendedPolicies);

        when(policyListRepository.findById(1L)).thenReturn(Optional.of(mockPolicy));
        when(policyListRepository.findById(2L)).thenReturn(Optional.of(mockPolicy));

        ResponseEntity<Map<String, Object>> response = notificationController.getNotificationDashboard();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> dashboard = response.getBody();

        System.out.println("Dashboard: " + dashboard);

        // 대시보드의 "memberNotifications" 값 확인
        List<Map<String, Object>> memberNotifications = (List<Map<String, Object>>) dashboard.get("memberNotifications");
        assertEquals(1, memberNotifications.size());
        assertEquals("Test Policy", memberNotifications.get(0).get("policyName"));
        assertEquals(mockPolicy.getApplyEndDate(), memberNotifications.get(0).get("applyEndDate"));

        // 대시보드의 "recommendedNotifications" 값 확인
        List<Map<String, Object>> recommendedNotifications = (List<Map<String, Object>>) dashboard.get("recommendedNotifications");
        assertEquals(1, recommendedNotifications.size());
        assertEquals("Test Policy", recommendedNotifications.get(0).get("policyName"));
        assertEquals(mockPolicy.getApplyEndDate(), recommendedNotifications.get(0).get("applyEndDate"));
    }
}
