package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.service.MemberService;
import com.chapter1.blueprint.member.service.NotificationService;
import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import com.chapter1.blueprint.policy.service.PolicyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberService memberService;
    private final PolicyListRepository policyListRepository;
    private final PolicyRecommendationService policyRecommendationService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PutMapping("/status")
    public ResponseEntity<String> updateNotificationStatus(@RequestBody Map<String, Object> request) {
        Long uid = memberService.getAuthenticatedUid();
        logger.debug("Updating notification status for UID: {}", uid);
        boolean notificationEnabled = (Boolean) request.get("notificationEnabled");

        notificationService.updateNotificationStatus(uid, notificationEnabled);

        return ResponseEntity.ok("Notification status updated successfully.");
    }

    @PutMapping("/{policyIdx}")
    public ResponseEntity<String> updateNotificationSettings(
            @PathVariable Long policyIdx,
            @RequestBody Map<String, Object> request) {
        Long uid = memberService.getAuthenticatedUid();
        boolean notificationEnabled = (Boolean) request.get("notificationEnabled");
        Date applyEndDate = (Date) request.get("applyEndDate");

        notificationService.saveOrUpdateNotification(uid, policyIdx, notificationEnabled, applyEndDate);

        return ResponseEntity.ok("Notification settings updated successfully.");
    }

    @DeleteMapping("/{policyIdx}")
    public ResponseEntity<String> deleteNotificationSettings(@PathVariable Long policyIdx) {
        Long uid = memberService.getAuthenticatedUid();
        notificationService.deleteNotification(uid, policyIdx);
        return ResponseEntity.ok("Notification settings deleted successfully.");
    }

    @GetMapping("/list/member")
    public ResponseEntity<?> getMemberDefinedNotifications() {
        Long uid = memberService.getAuthenticatedUid();
        List<PolicyAlarm> memberNotifications = notificationService.getMemberNotifications(uid);
        return ResponseEntity.ok(new SuccessResponse(memberNotifications));
    }

    @GetMapping("/list/recommended")
    public ResponseEntity<?> getRecommendedNotifications() {
        Long uid = memberService.getAuthenticatedUid();
        List<PolicyAlarm> recommendedNotifications = notificationService.getRecommendedNotifications(uid);
        return ResponseEntity.ok(new SuccessResponse(recommendedNotifications));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getNotificationDashboard() {
        Long uid = memberService.getAuthenticatedUid();

        // 사용자 설정 알림
        List<PolicyAlarm> memberNotifications = notificationService.getMemberNotifications(uid);

        // 추천된 정책 알림
        List<PolicyAlarm> recommendedNotifications = notificationService.getRecommendedNotifications(uid);

        // 추천된 정책 (실시간 추천)
        List<PolicyDetailFilter> recommendedPolicies = policyRecommendationService.getRecommendedPolicies(uid);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("memberNotifications", memberNotifications.stream()
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx())
                            .orElse(null);
                    if (policy != null && policy.getName() != null && policy.getApplyEndDate() != null) {
                        return Map.of(
                                "policyName", policy.getName(),
                                "applyEndDate", policy.getApplyEndDate()
                        );
                    }
                    return null;
                })
                .filter(map -> map != null)
                .toList());

        dashboard.put("recommendedNotifications", recommendedNotifications.stream()
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx())
                            .orElse(null);
                    if (policy != null && policy.getName() != null && policy.getApplyEndDate() != null) {
                        return Map.of(
                                "policyName", policy.getName(),
                                "applyEndDate", policy.getApplyEndDate()
                        );
                    }
                    return null;
                })
                .filter(map -> map != null)
                .toList());

        dashboard.put("recommendedPolicies", recommendedPolicies.stream()
                .map(policy -> {
                    if (policy.getTarget() != null && policy.getApplyEndDate() != null) {
                        return Map.of(
                                "policyName", policy.getTarget(),
                                "applyEndDate", policy.getApplyEndDate()
                        );
                    }
                    return null;
                })
                .filter(map -> map != null)
                .toList());

        return ResponseEntity.ok(dashboard);
    }

}
