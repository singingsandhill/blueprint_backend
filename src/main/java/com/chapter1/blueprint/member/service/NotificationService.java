package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.exception.codes.ErrorCode;
import com.chapter1.blueprint.exception.codes.ErrorCodeException;
import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.domain.PolicyAlarmType;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import com.chapter1.blueprint.policy.service.PolicyDetailService;
import jakarta.persistence.EntityNotFoundException;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PolicyAlarmRepository policyAlarmRepository;
    private final MemberRepository memberRepository;
    private final PolicyListRepository policyListRepository;
    private final PolicyDetailService policyDetailService;
    private final EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // 알림 상태 조회
    @Transactional(readOnly = true)
    public boolean getNotificationStatus(Long uid) {
        logger.info("Fetching notification status for UID: {}", uid);
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND));

        logger.info("Notification status for UID {}: {}", uid, member.getNotificationStatus());
        return member.getNotificationStatus();
    }

    // 알림 상태 업데이트
    @Transactional
    public void updateNotificationStatus(Long uid, boolean enabled) {
        logger.info("Updating notification status for UID: {}", uid);

        try {
            // Member 엔티티 업데이트
            Member member = memberRepository.findById(uid)
                    .orElseThrow(() -> new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND));
            member.setNotificationStatus(enabled);
            memberRepository.save(member);
            logger.info("Updated Member.notificationStatus for UID: {}", uid);

            // 추천 정책에 대한 PolicyAlarm 업데이트
            List<PolicyList> recommendedPolicies = policyDetailService.recommendPolicy(uid);

            if (enabled) {
                logger.info("Enabling notifications for {} recommended policies for UID: {}", recommendedPolicies.size(), uid);
                for (PolicyList policy : recommendedPolicies) {
                    PolicyAlarm alarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policy.getIdx());
                    if (alarm == null) {
                        alarm = PolicyAlarm.builder()
                                .uid(uid)
                                .policyIdx(policy.getIdx())
                                .notificationEnabled(true)
                                .alarmType(PolicyAlarmType.RECOMMENDED.getType())
                                .applyEndDate(policy.getApplyEndDate())
                                .isRead(false)
                                .build();
                        policyAlarmRepository.save(alarm);
                    } else {
                        alarm.setNotificationEnabled(true);
                        alarm.setApplyEndDate(policy.getApplyEndDate());
                        policyAlarmRepository.save(alarm);
                    }
                }
            } else {
                logger.info("Disabling notifications for all recommended policies for UID: {}", uid);
                List<PolicyAlarm> alarms = policyAlarmRepository.findByUidAndAlarmType(uid, PolicyAlarmType.RECOMMENDED.getType());
                for (PolicyAlarm alarm : alarms) {
                    alarm.setNotificationEnabled(false);
                    policyAlarmRepository.save(alarm);
                }
            }

            logger.info("Notification status updated successfully for UID: {}", uid);

        } catch (ErrorCodeException e) {
            logger.error("Error while updating notification status for UID: {}", uid, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while updating notification status for UID: {}", uid, e);
            throw new ErrorCodeException(ErrorCode.NOTIFICATION_UPDATE_FAILED);
        }
    }

    // 알림 저장 또는 업데이트
    @Transactional
    public void saveOrUpdateNotification(Long uid, Long policyIdx, boolean notificationEnabled) {
        try {
            PolicyList policy = policyListRepository.findById(policyIdx)
                    .orElseThrow(() -> new ErrorCodeException(ErrorCode.POLICY_NOT_FOUND));

            PolicyAlarm existingAlarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);

            if (existingAlarm == null) {
                logger.info("Creating new MEMBER_DEFINED PolicyAlarm for UID: {}, PolicyIdx: {}", uid, policyIdx);
                PolicyAlarm newAlarm = PolicyAlarm.builder()
                        .uid(uid)
                        .policyIdx(policyIdx)
                        .notificationEnabled(notificationEnabled)
                        .alarmType(PolicyAlarmType.MEMBER_DEFINED.getType())
                        .applyEndDate(policy.getApplyEndDate()) // applyEndDate가 null일 경우 처리
                        .isRead(false)
                        .build();
                policyAlarmRepository.save(newAlarm);
            } else {
                logger.info("Updating MEMBER_DEFINED PolicyAlarm for UID: {}, PolicyIdx: {}", uid, policyIdx);
                existingAlarm.setNotificationEnabled(notificationEnabled);
                existingAlarm.setAlarmType(PolicyAlarmType.MEMBER_DEFINED.getType());
                existingAlarm.setApplyEndDate(policy.getApplyEndDate()); // applyEndDate가 null일 경우 처리
                if (!notificationEnabled) {
                    existingAlarm.setIsRead(false);
                }
                policyAlarmRepository.save(existingAlarm);
            }
        } catch (Exception e) {
            logger.error("Unexpected error while saving or updating MEMBER_DEFINED notification for UID: {}, PolicyIdx: {}", uid, policyIdx, e);
            throw new ErrorCodeException(ErrorCode.NOTIFICATION_UPDATE_FAILED);
        }
    }

    // 알림 삭제
    @Transactional
    public void deleteNotification(Long uid, Long policyIdx) {
        try {
            PolicyAlarm alarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);
            if (alarm == null) {
                throw new ErrorCodeException(ErrorCode.POLICY_ALARM_NOT_FOUND);
            }

            policyAlarmRepository.delete(alarm);
            logger.info("Notification deleted for UID: {}, PolicyIdx: {}", uid, policyIdx);
        } catch (ErrorCodeException e) {
            logger.error("Error during deletion of notification for UID: {}, PolicyIdx: {}", uid, policyIdx, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during notification deletion for UID: {}, PolicyIdx: {}", uid, policyIdx, e);
            throw new ErrorCodeException(ErrorCode.NOTIFICATION_DELETION_FAILED);
        }
    }

    // 사용자 정의 알림 가져오기
    public List<PolicyAlarm> getMemberNotifications(Long uid) {
        return policyAlarmRepository.findByUidAndAlarmType(uid, "MEMBER_DEFINED")
                .stream()
                .filter(alarm -> Boolean.TRUE.equals(alarm.getNotificationEnabled()))
                .collect(Collectors.toList());
    }

    // 추천 알림 가져오기
    public List<PolicyAlarm> getRecommendedNotifications(Long uid) {
        return policyAlarmRepository.findByUidAndAlarmType(uid, "RECOMMENDED")
                .stream()
                .filter(alarm -> alarm.getApplyEndDate() != null)
                .collect(Collectors.toList());
    }

    // 알림 읽음 상태 업데이트
    @Transactional
    public void markNotificationAsRead(Long uid, Long policyIdx) {
        PolicyAlarm alarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);
        if (alarm == null) {
            logger.error("Notification not found for UID: {}, PolicyIdx: {}", uid, policyIdx);
            throw new ErrorCodeException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        logger.info("Fetched PolicyAlarm: {}", alarm);

        alarm.setIsRead(true);
        policyAlarmRepository.save(alarm);
    }

    public List<Map<String, Object>> formatNotifications(List<PolicyAlarm> alarms, DateTimeFormatter formatter) {
        return alarms.stream()
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
                        map.put("isRead", alarm.getIsRead());
                        map.put("policyIdx", alarm.getPolicyIdx());
                        return map;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Push 알림 조회
    @Transactional(readOnly = true)
    public List<Map<String, String>> getPushNotifications(Long uid) {
        log.info("Fetching push notifications for UID: {}", uid);

        // UID로 알림 조회
        List<PolicyAlarm> alarms = policyAlarmRepository.findByUid(uid);
        if (alarms.isEmpty()) {
            log.info("No notifications found for UID: {}", uid);
            return Collections.emptyList();
        }

        log.info("Total alarms fetched: {}", alarms.size());

        // 필터링: ApplyEndDate가 null인 경우 제외하고 날짜 조건 만족 여부 확인
        return alarms.stream()
                .filter(alarm -> {
                    Date applyEndDate = alarm.getApplyEndDate();
                    if (applyEndDate == null) {
                        log.info("Alarm ID: {}, ApplyEndDate is null, skipping...", alarm.getPolicyIdx());
                        return false;
                    }

                    // 날짜 비교: 3일 전 또는 1일 전 조건 만족
                    boolean isEmailNotification = isThreeDaysBefore(applyEndDate);
                    boolean isDayBeforeNotification = isOneDayBefore(applyEndDate);

                    log.info("Alarm ID: {}, ApplyEndDate: {}, AlarmType: {}, IsEmail: {}, IsDayBefore: {}",
                            alarm.getPolicyIdx(), applyEndDate, alarm.getAlarmType(), isEmailNotification, isDayBeforeNotification);

                    return isEmailNotification || isDayBeforeNotification;
                })
                .map(alarm -> {
                    PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx())
                            .orElseThrow(() -> {
                                log.error("Policy not found for PolicyIdx: {}", alarm.getPolicyIdx());
                                throw new ErrorCodeException(ErrorCode.POLICY_NOT_FOUND);
                            });

                    // 알림 메시지 작성
                    Map<String, String> message = new HashMap<>();
                    message.put("policyName", policy.getName());
                    message.put("applyEndDate", formatDate(policy.getApplyEndDate()));
                    message.put("pushDate", formatDate(new Date())); // 현재 날짜를 pushDate로 추가

                    // 라벨 추가: 날짜 조건에 따라 "이메일 발송" 또는 "마감 하루 전"
                    if (isOneDayBefore(alarm.getApplyEndDate())) {
                        message.put("message", "마감 하루 전");
                    } else if (isThreeDaysBefore(alarm.getApplyEndDate())) {
                        message.put("message", "이메일 발송");
                    }

                    return message;
                })
                .toList();
    }

    // 알림 처리
    public void processAllPolicyAlarms() {
        List<PolicyAlarm> alarms = policyAlarmRepository.findByNotificationEnabled(true);
        for (PolicyAlarm alarm : alarms) {
            processPolicyAlarm(alarm);
        }
    }

    private void processPolicyAlarm(PolicyAlarm alarm) {
        try {
            PolicyList policy = policyListRepository.findById(alarm.getPolicyIdx())
                    .orElseThrow(() -> new ErrorCodeException(ErrorCode.POLICY_NOT_FOUND));

            if (isThreeDaysBefore(policy.getApplyEndDate()) && alarm.getSendDate() == null) {
                sendEmailNotification(policy, alarm);
            }

            if (isOneDayBefore(policy.getApplyEndDate())) {
                createPushNotification(alarm, "마감 하루 전");
            }
        } catch (Exception e) {
            logger.error("Error processing PolicyAlarm for UID: {}, PolicyIdx: {}", alarm.getUid(), alarm.getPolicyIdx(), e);
        }
    }

    // 이메일 발송
    private void sendEmailNotification(PolicyList policy, PolicyAlarm alarm) {
        Member member = memberRepository.findById(alarm.getUid())
                .orElseThrow(() -> new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND));

        emailService.sendNotificationEmail(
                member.getEmail(),
                policy.getName(),
                (java.sql.Date) policy.getApplyEndDate(),
                policy.getIdx()
        );

        alarm.setSendDate(new Date());
        policyAlarmRepository.save(alarm);

        createPushNotification(alarm, "이메일 발송");
    }

    // Push 알림 생성
    private void createPushNotification(PolicyAlarm alarm, String label) {
        logger.info("Push notification created: Label: {}, Policy Name: {}, Push Date: {}",
                label,
                alarm.getPolicyIdx(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private String formatDate(Date date) {
        return date != null
                ? date.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "상시";
    }

    private boolean isThreeDaysBefore(Date date) {
        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(), date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        log.debug("Checking 3 days before: Today: {}, TargetDate: {}, Difference: {}", LocalDate.now(), date, daysDiff);
        return daysDiff == 3;
    }

    private boolean isOneDayBefore(Date date) {
        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(), date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        log.debug("Checking 1 day before: Today: {}, TargetDate: {}, Difference: {}", LocalDate.now(), date, daysDiff);
        return daysDiff == 1;
    }
}
