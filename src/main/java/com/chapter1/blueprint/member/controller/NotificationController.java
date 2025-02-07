package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;

import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import com.chapter1.blueprint.member.service.MemberService;
import com.chapter1.blueprint.member.service.NotificationService;
import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import com.chapter1.blueprint.exception.codes.ErrorCodeException;
import com.chapter1.blueprint.exception.codes.ErrorCode;

import com.chapter1.blueprint.policy.service.PolicyDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberService memberService;
    private final PolicyListRepository policyListRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @GetMapping("/status")
    public ResponseEntity<SuccessResponse> getNotificationStatus() {
        Long uid = memberService.getAuthenticatedUid();
        logger.info("Fetching notification status for UID: {}", uid);

        boolean notificationStatus = notificationService.getNotificationStatus(uid);

        logger.info("Notification status fetched successfully for UID: {} with status: {}", uid, notificationStatus);

        return ResponseEntity.ok(new SuccessResponse(Map.of("notificationEnabled", notificationStatus)));
    }

    @PutMapping("/status")
    public ResponseEntity<SuccessResponse> updateNotificationStatus(@RequestBody Map<String, Object> request) {
        Long uid = memberService.getAuthenticatedUid();
        logger.info("Retrieved UID: {}", uid);
        logger.info("Request body: {}", request);

        boolean notificationEnabled = (Boolean) request.get("notificationEnabled");

        logger.info("Calling notificationService.updateNotificationStatus with uid: {} and enabled: {}", uid, notificationEnabled);

        notificationService.updateNotificationStatus(uid, notificationEnabled);

        boolean updatedNotificationStatus = notificationService.getNotificationStatus(uid);

        logger.info("Notification status updated successfully for UID: {} with new status: {}", uid, updatedNotificationStatus);

        return ResponseEntity.ok(new SuccessResponse(Map.of("notificationEnabled", updatedNotificationStatus)));
    }


    @PutMapping("/{policyIdx}")
    public ResponseEntity<SuccessResponse> updateNotificationSettings(

            @PathVariable Long policyIdx,
            @RequestBody Map<String, Object> request) {
        Long uid = memberService.getAuthenticatedUid();
        boolean notificationEnabled = (Boolean) request.get("notificationEnabled");

        logger.info("Calling notificationService.saveOrUpdateNotification with uid: {}, policyIdx: {}, enabled: {}",
                uid, policyIdx, notificationEnabled);

        notificationService.saveOrUpdateNotification(uid, policyIdx, notificationEnabled);

        return ResponseEntity.ok(new SuccessResponse("Notification settings updated successfully."));
    }

    @DeleteMapping("/{policyIdx}")
    public ResponseEntity<String> deleteNotificationSettings(@PathVariable Long policyIdx) {
        Long uid = memberService.getAuthenticatedUid();
        notificationService.deleteNotification(uid, policyIdx);
        return ResponseEntity.ok("Notification settings deleted successfully.");
    }

    @GetMapping("/list/member")
    public ResponseEntity<SuccessResponse> getMemberDefinedNotifications() {
        Long uid = memberService.getAuthenticatedUid();
        logger.info("Fetching member-defined notifications for UID: {}", uid);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, Object>> memberNotifications = notificationService.getMemberNotifications(uid).stream()
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx()).orElse(null);
                    if (policy != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("policyName", policy.getName());
                        map.put("applyEndDate", alarm.getApplyEndDate() != null
                                ? alarm.getApplyEndDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(formatter)
                                : "상시");

                        map.put("policyIdx", alarm.getPolicyIdx());
                        map.put("notification_enabled", alarm.getNotificationEnabled());

                        return map;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new SuccessResponse(memberNotifications));
    }

    @GetMapping("/list/recommended")
    public ResponseEntity<SuccessResponse> getRecommendedNotifications() {
        Long uid = memberService.getAuthenticatedUid();
        logger.info("Fetching recommended notifications for UID: {}", uid);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, Object>> recommendedNotifications = notificationService.getRecommendedNotifications(uid).stream()
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx()).orElse(null);
                    if (policy != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("policyName", policy.getName());
                        map.put("applyEndDate", alarm.getApplyEndDate() != null
                                ? alarm.getApplyEndDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(formatter)
                                : "상시");

                        map.put("policyIdx", alarm.getPolicyIdx());
                        map.put("notification_enabled", alarm.getNotificationEnabled());

                        return map;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new SuccessResponse(recommendedNotifications));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse> getNotificationDashboard() {
        Long uid = memberService.getAuthenticatedUid();
        logger.info("Fetching dashboard data for UID: {}", uid);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 사용자 설정 알림
        List<PolicyAlarm> memberNotifications = notificationService.getMemberNotifications(uid);
        List<Map<String, Object>> formattedMemberNotifications = memberNotifications.stream()
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx()).orElse(null);
                    if (policy != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("policyName", policy.getName());
                        map.put("applyEndDate", alarm.getApplyEndDate() != null
                                ? alarm.getApplyEndDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(formatter)
                                : "상시");

                        map.put("policyIdx", alarm.getPolicyIdx());
                        map.put("notification_enabled", alarm.getNotificationEnabled());

                        return map;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 추천된 정책 알림
        List<PolicyAlarm> recommendedNotifications = notificationService.getRecommendedNotifications(uid);
        List<Map<String, Object>> formattedRecommendedNotifications = recommendedNotifications.stream()
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx()).orElse(null);
                    if (policy != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("policyName", policy.getName());
                        map.put("applyEndDate", alarm.getApplyEndDate() != null
                                ? alarm.getApplyEndDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(formatter)
                                : "상시");

                        map.put("policyIdx", alarm.getPolicyIdx());
                        map.put("notification_enabled", alarm.getNotificationEnabled());

                        return map;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("memberNotifications", formattedMemberNotifications);
        dashboard.put("recommendedNotifications", formattedRecommendedNotifications);

        return ResponseEntity.ok(new SuccessResponse(dashboard));
    }

    @PutMapping("/read/{policyIdx}")
    public ResponseEntity<SuccessResponse> markNotificationAsRead(@PathVariable Long policyIdx) {
        Long uid = memberService.getAuthenticatedUid();
        logger.info("Marking notification as read for UID: {}, PolicyIdx: {}", uid, policyIdx);

        notificationService.markNotificationAsRead(uid, policyIdx);

        return ResponseEntity.ok(new SuccessResponse("Notification marked as read."));
    }

    @GetMapping("/push")
    public ResponseEntity<SuccessResponse> getPushNotifications() {

        Long uid = memberService.getAuthenticatedUid();
        logger.info("Fetching push notifications for UID: {}", uid);

        var pushMessages = notificationService.getPushNotifications(uid);
        return ResponseEntity.ok(new SuccessResponse(pushMessages));
    }
}
