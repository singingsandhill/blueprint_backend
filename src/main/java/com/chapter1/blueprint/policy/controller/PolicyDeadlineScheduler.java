package com.chapter1.blueprint.policy.controller;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import com.chapter1.blueprint.member.service.EmailService;
import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import com.chapter1.blueprint.policy.service.PolicyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicyDeadlineScheduler {

    private final PolicyAlarmRepository policyAlarmRepository;
    private final PolicyListRepository policyListRepository;
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final PolicyRecommendationService policyRecommendationService; // 추천 서비스 추가
    private static final Logger logger = LoggerFactory.getLogger(PolicyDeadlineScheduler.class);

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkPolicyDeadline() {
        logger.info("Starting policy deadline check...");

        // 사용자가 설정한 정책 알림
        checkMemberSetPolicyAlarms();

        // 추천 정책 알림
        checkRecommendedPolicyAlarms();
    }

    private void checkMemberSetPolicyAlarms() {
        List<PolicyAlarm> notificationSettings = policyAlarmRepository.findByNotificationEnabled(true);

        for (PolicyAlarm setting : notificationSettings) {
            PolicyList policy = policyListRepository.findById(setting.getPolicyIdx()).orElse(null);

            if (policy != null) {
                Date applyEndDate = (Date) policy.getApplyEndDate();

                if (isThreeDaysBefore(applyEndDate)) {
                    sendNotificationToMemberForPolicy(policy, setting.getUid());
                }
            }
        }
    }

    private void checkRecommendedPolicyAlarms() {
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            List<PolicyDetailFilter> recommendedPolicies = policyRecommendationService.getRecommendedPolicies(member.getUid());

            for (PolicyDetailFilter policy : recommendedPolicies) {
                if (isThreeDaysBefore(policy.getApplyEndDate())) {
                    sendRecommendationEmail(member, policy);
                }
            }
        }
    }

    private boolean isThreeDaysBefore(Date applyEndDate) {
        LocalDate endDate = applyEndDate.toLocalDate();
        LocalDate currentDate = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(currentDate, endDate);

        return daysBetween == 3;
    }

    private void sendNotificationToMemberForPolicy(PolicyList policyList, Long uid) {
        String policyName = policyList.getName();
        Date endDate = (Date) policyList.getApplyEndDate();
        Long idx = policyList.getIdx();

        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with UID: " + uid));

        emailService.sendNotificationEmail(member.getEmail(), policyName, endDate, idx);
        logger.info("Email sent to: {} for policy: {}", member.getEmail(), policyName);
    }

    private void sendRecommendationEmail(Member member, PolicyDetailFilter policy) {
        PolicyList policyList = policyListRepository.findById(policy.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policy.getIdx()));

        String policyName = policyList.getName();
        Date endDate = policy.getApplyEndDate();
        Long idx = policy.getIdx();

        emailService.sendNotificationEmail(member.getEmail(), policyName, endDate, idx);
        logger.info("Recommendation email sent to: {} for policy: {}", member.getEmail(), policyName);
    }

}
