package com.chapter1.blueprint.policy.controller;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import com.chapter1.blueprint.member.service.EmailService;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


@RequiredArgsConstructor
public class PolicyDeadlineScheduler {

    private final PolicyAlarmRepository policyAlarmRepository;
    private final PolicyListRepository policyListRepository;
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private static final Logger logger = LoggerFactory.getLogger(PolicyDeadlineScheduler.class);

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void checkPolicyDeadline() {
        List<PolicyAlarm> notificationSettings = policyAlarmRepository.findByNotificationEnabled(true);

        for (PolicyAlarm setting : notificationSettings) {
            PolicyList policy = policyListRepository.findById(setting.getPolicyIdx()).orElse(null);

            if (policy != null) {
                Date applyEndDate = (Date) policy.getApplyEndDate();

                if (isThreeDaysBefore(applyEndDate)) {
                    sendNotificationToUsersForPolicy(policy);
                }
            }
        }
    }

    private boolean isThreeDaysBefore(Date applyEndDate) {
        LocalDate endDate = applyEndDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(currentDate, endDate);

        return daysBetween == 3;
    }

    public void sendNotificationToUsersForPolicy(PolicyList policyList) {
        String policyName = policyList.getName();
        Date endDate = (Date) policyList.getApplyEndDate();
        Long idx = policyList.getIdx();

        List<Member> users = memberRepository.findByNotificationStatusTrue();

        for (Member user : users) {
            emailService.sendNotificationEmail(user.getEmail(), policyName, endDate, idx);
            logger.info("Email sent to: {} for policy: {}", user.getEmail(), policyName);
        }
    }


}
