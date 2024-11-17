package com.chapter1.blueprint.policy.controller;

import com.chapter1.blueprint.exception.codes.ErrorCode;
import com.chapter1.blueprint.exception.codes.ErrorCodeException;

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

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkPolicyDeadline() {
        logger.info("Starting policy deadline check...");

        try {
            // 사용자가 설정한 정책 알림
            checkMemberSetPolicyAlarms();

            // 추천 정책 알림
            checkRecommendedPolicyAlarms();

            logger.info("Policy deadline check completed successfully.");
        } catch (Exception e) {
            logger.error("Unexpected error during policy deadline check: {}", e.getMessage(), e);
            throw new ErrorCodeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void checkMemberSetPolicyAlarms() {
        List<PolicyAlarm> notificationSettings = policyAlarmRepository.findByNotificationEnabled(true);

        for (PolicyAlarm setting : notificationSettings) {
            try {
                PolicyList policy = policyListRepository.findById(setting.getPolicyIdx())
                        .orElseThrow(() -> new ErrorCodeException(ErrorCode.POLICY_NOT_FOUND));

                Date applyEndDate = (Date) policy.getApplyEndDate();

                if (isThreeDaysBefore(applyEndDate)) {
                    logger.info("Sending notification for UID: {}, Policy: {}", setting.getUid(), policy.getName());
                    sendNotificationToMemberForPolicy(policy, setting.getUid());
                }
            } catch (ErrorCodeException e) {
                logger.error("Policy not found or other error for UID: {}, PolicyIdx: {}", setting.getUid(), setting.getPolicyIdx(), e);
            } catch (Exception e) {
                logger.error("Unexpected error for UID: {}, PolicyIdx: {}", setting.getUid(), setting.getPolicyIdx(), e);
                throw new ErrorCodeException(ErrorCode.NOTIFICATION_UPDATE_FAILED);
            }
        }
    }

    private void checkRecommendedPolicyAlarms() {
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            try {
                List<PolicyDetailFilter> recommendedPolicies = policyRecommendationService.getRecommendedPolicies(member.getUid());

                for (PolicyDetailFilter policy : recommendedPolicies) {
                    if (isThreeDaysBefore(policy.getApplyEndDate())) {
                        logger.info("Sending recommendation email for Member: {}, Policy: {}", member.getUid(), policy.getTarget());
                        sendRecommendationEmail(member, policy);
                    }
                }
            } catch (ErrorCodeException e) {
                logger.error("Error during recommendation processing for Member UID: {}", member.getUid(), e);
            } catch (Exception e) {
                logger.error("Unexpected error for Member UID: {}", member.getUid(), e);
                throw new ErrorCodeException(ErrorCode.RECOMMENDED_POLICY_EMAIL_FAILED);
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
        try {
            String policyName = policyList.getName();
            Long policyIdx = policyList.getIdx();

            Member member = memberRepository.findById(uid)
                    .orElseThrow(() -> new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND));

            PolicyAlarm policyAlarm = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);
            if (policyAlarm == null) {
                policyAlarm = new PolicyAlarm();
            } else if (policyAlarm.getSendDate() != null) {
                logger.info("Email already sent for UID: {}, PolicyIdx: {}", uid, policyIdx);
                return;
            }

            emailService.sendNotificationEmail(member.getEmail(), policyName, (Date) policyList.getApplyEndDate(), policyIdx);
            logger.info("Email sent to: {} for policy: {}", member.getEmail(), policyName);

            if (policyAlarm.getIdx() == null) {
                policyAlarm.setUid(member.getUid());
                policyAlarm.setPolicyIdx(policyIdx);
                policyAlarm.setApplyEndDate(policyList.getApplyEndDate());
            }

            policyAlarm.setSendDate(new java.util.Date());
            policyAlarmRepository.save(policyAlarm);

            String pushMessage = "마감 3일 전, '" + policyName + "' 이메일 발송되었습니다";
            sendPushNotification(member.getUid(), pushMessage);

            logger.info("Email sent and push notification created for UID: {}, PolicyIdx: {}", uid, policyIdx);
        } catch (ErrorCodeException e) {
            logger.error("Error during notification email sending for UID: {}, PolicyIdx: {}", uid, policyList.getIdx(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during email sending for UID: {}, PolicyIdx: {}", uid, policyList.getIdx(), e);
            throw new ErrorCodeException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    private void sendRecommendationEmail(Member member, PolicyDetailFilter policy) {
        try {
            PolicyList policyList = policyListRepository.findById(policy.getIdx())
                    .orElseThrow(() -> new ErrorCodeException(ErrorCode.POLICY_NOT_FOUND));

            String policyName = policyList.getName();
            Long policyIdx = policy.getIdx();

            PolicyAlarm policyAlarm = policyAlarmRepository.findByUidAndPolicyIdx(member.getUid(), policyIdx);
            if (policyAlarm == null) {
                policyAlarm = new PolicyAlarm();
            }

            if (policyAlarm.getSendDate() != null) {
                logger.info("Recommendation email already sent for UID: {}, PolicyIdx: {}", member.getUid(), policyIdx);
                return;
            }

            emailService.sendNotificationEmail(member.getEmail(), policyName, (Date) policyList.getApplyEndDate(), policyIdx);
            logger.info("Recommendation email sent to: {} for policy: {}", member.getEmail(), policyName);

            if (policyAlarm.getIdx() == null) {
                policyAlarm.setUid(member.getUid());
                policyAlarm.setPolicyIdx(policyIdx);
                policyAlarm.setApplyEndDate(policyList.getApplyEndDate());
            }

            policyAlarm.setSendDate(new java.util.Date());
            policyAlarmRepository.save(policyAlarm);

            String pushMessage = "마감 3일 전, '" + policyName + "' 이메일 발송되었습니다";
            sendPushNotification(member.getUid(), pushMessage);

            logger.info("Recommendation email sent and push notification created for UID: {}, PolicyIdx: {}", member.getUid(), policyIdx);
        } catch (ErrorCodeException e) {
            logger.error("Error during recommendation email sending for UID: {}, PolicyIdx: {}", member.getUid(), policy.getIdx(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during recommendation email sending for UID: {}, PolicyIdx: {}", member.getUid(), policy.getIdx(), e);
            throw new ErrorCodeException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    private void sendPushNotification(Long uid, String message) {
        logger.info("Push notification sent to UID: {} with message: {}", uid, message);
    }
}
