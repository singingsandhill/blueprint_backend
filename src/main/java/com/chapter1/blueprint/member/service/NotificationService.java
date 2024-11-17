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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PolicyAlarmRepository policyAlarmRepository;
    private final MemberRepository memberRepository;
    private final PolicyListRepository policyListRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Transactional
    public void updateNotificationStatus(Long uid, boolean enabled) {
        logger.info("Updating notification status for UID: {}", uid);

        try {
            // 1. Member 엔티티 업데이트
            Member member = memberRepository.findById(uid)
                    .orElseThrow(() -> new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND));
            member.setNotificationStatus(enabled);
            memberRepository.save(member);
            logger.info("Updated Member.notificationStatus for UID: {}", uid);

            // 2. PolicyAlarm 엔티티 업데이트
            List<PolicyAlarm> alarms = policyAlarmRepository.findByUidAndAlarmType(uid, PolicyAlarmType.RECOMMENDED.getType());
            logger.info("Found {} RECOMMENDED PolicyAlarms for UID: {}", alarms.size(), uid);

            if (alarms.isEmpty()) {
                logger.info("No existing RECOMMENDED PolicyAlarm. Creating a new one for UID: {}", uid);
                PolicyAlarm newAlarm = PolicyAlarm.builder()
                        .uid(uid)
                        .notificationEnabled(enabled)
                        .policyIdx(0L)
                        .alarmType(PolicyAlarmType.RECOMMENDED.getType())
                        .build();
                policyAlarmRepository.save(newAlarm);
                logger.info("Created new RECOMMENDED PolicyAlarm for UID: {}", uid);
            } else {
                logger.info("Updating {} RECOMMENDED PolicyAlarms for UID: {}", alarms.size(), uid);
                alarms.forEach(alarm -> alarm.setNotificationEnabled(enabled));
                policyAlarmRepository.saveAll(alarms);
                logger.info("Updated existing RECOMMENDED PolicyAlarms for UID: {}", uid);
            }
        } catch (ErrorCodeException e) {
            logger.error("Error while updating notification status for UID: {}", uid, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while updating notification status for UID: {}", uid, e);
            throw new ErrorCodeException(ErrorCode.NOTIFICATION_UPDATE_FAILED);
        }
    }

    @Transactional
    public void saveOrUpdateNotification(Long uid, Long policyIdx, boolean notificationEnabled) {
        try {
            PolicyAlarm existingAlarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);

            if (existingAlarm == null) {
                logger.info("Creating new MEMBER_DEFINED PolicyAlarm for UID: {}, PolicyIdx: {}", uid, policyIdx);
                PolicyAlarm newAlarm = PolicyAlarm.builder()
                        .uid(uid)
                        .policyIdx(policyIdx)
                        .notificationEnabled(notificationEnabled)
                        .alarmType(PolicyAlarmType.MEMBER_DEFINED.getType())
                        .build();
                policyAlarmRepository.save(newAlarm);
            } else {
                logger.info("Updating MEMBER_DEFINED PolicyAlarm for UID: {}, PolicyIdx: {}", uid, policyIdx);
                existingAlarm.setNotificationEnabled(notificationEnabled);
                existingAlarm.setAlarmType(PolicyAlarmType.MEMBER_DEFINED.getType());
                policyAlarmRepository.save(existingAlarm);
            }
        } catch (Exception e) {
            logger.error("Unexpected error while saving or updating MEMBER_DEFINED notification for UID: {}, PolicyIdx: {}", uid, policyIdx, e);
            throw new ErrorCodeException(ErrorCode.NOTIFICATION_UPDATE_FAILED);
        }
    }


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


    public List<PolicyAlarm> getMemberNotifications(Long uid) {
        return policyAlarmRepository.findByUidAndAlarmType(uid, "MEMBER_DEFINED")
                .stream()
                .filter(alarm -> alarm.getNotificationEnabled() != null && alarm.getNotificationEnabled())
                .collect(Collectors.toList());
    }

    public List<PolicyAlarm> getRecommendedNotifications(Long uid) {
        return policyAlarmRepository.findByUidAndAlarmType(uid, "RECOMMENDED")
                .stream()
                .filter(alarm -> alarm.getApplyEndDate() != null)
                .collect(Collectors.toList());
    }

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
}
