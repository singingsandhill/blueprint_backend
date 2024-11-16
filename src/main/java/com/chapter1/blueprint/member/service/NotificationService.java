package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PolicyAlarmRepository policyAlarmRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void updateNotificationStatus(Long uid, boolean enabled) {
        logger.debug("Updating notifications for UID: {}, enabled: {}", uid, enabled);
        List<PolicyAlarm> alarms = policyAlarmRepository.findByUid(uid);
        if (alarms.isEmpty()) {
            logger.error("No PolicyAlarm found for UID: {}", uid);
            throw new IllegalArgumentException("PolicyAlarm not found for UID: " + uid);
        }

        for (PolicyAlarm alarm : alarms) {
            alarm.setNotificationEnabled(enabled);
        }

        policyAlarmRepository.saveAll(alarms);
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
