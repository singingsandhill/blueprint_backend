package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PolicyAlarmRepository policyAlarmRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void updateNotificationStatus(Long uid, boolean enabled) {
        log.info("Step 5 - Starting updateNotificationStatus for UID: {}", uid);

        try {
            List<PolicyAlarm> alarms = policyAlarmRepository.findByUid(uid);
            log.info("Step 6 - Found {} PolicyAlarms for UID: {}", alarms.size(), uid);

            if (alarms.isEmpty()) {
                log.info("Step 7a - No existing PolicyAlarm. Creating new one for UID: {}", uid);
                PolicyAlarm newAlarm = PolicyAlarm.builder()
                        .uid(uid)
                        .notificationEnabled(enabled)
                        .policyIdx(0L)  // 또는 적절한 기본값
                        .alarmType("MEMBER_DEFINED")  // 알람 타입 설정
                        .build();
                policyAlarmRepository.save(newAlarm);
                log.info("Step 8a - Successfully created new PolicyAlarm");
            } else {
                log.info("Step 7b - Updating {} existing PolicyAlarms", alarms.size());
                for (PolicyAlarm alarm : alarms) {
                    alarm.setNotificationEnabled(enabled);
                }
                policyAlarmRepository.saveAll(alarms);
                log.info("Step 8b - Successfully updated existing PolicyAlarms");
            }
        } catch (Exception e) {
            log.error("Error in updateNotificationStatus", e);
            throw e;
        }
    }

    public void saveOrUpdateNotification(Long uid, Long policyIdx, boolean notificationEnabled, Date applyEndDate) {
        PolicyAlarm alarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);

        if (alarm == null) {
            alarm = new PolicyAlarm();
            alarm.setUid(uid);
            alarm.setPolicyIdx(policyIdx);
            alarm.setNotificationEnabled(notificationEnabled);
            alarm.setApplyEndDate(applyEndDate);
        } else {
            alarm.setNotificationEnabled(notificationEnabled);
            alarm.setApplyEndDate(applyEndDate);
        }

        policyAlarmRepository.save(alarm);
    }

    public void deleteNotification(Long uid, Long policyIdx) {
        PolicyAlarm alarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);

        if (alarm == null) {
            throw new IllegalArgumentException("Notification not found for uid: " + uid + ", policyIdx: " + policyIdx);
        }

        policyAlarmRepository.delete(alarm);
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
}
