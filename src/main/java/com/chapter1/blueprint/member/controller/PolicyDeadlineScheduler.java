package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.member.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class PolicyDeadlineScheduler {

    private final NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(PolicyDeadlineScheduler.class);

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkPolicyDeadline() {
        logger.info("Starting policy deadline check...");
        try {
            notificationService.processAllPolicyAlarms();
            logger.info("Policy deadline check completed successfully.");
        } catch (Exception e) {
            logger.error("Error during policy deadline check", e);
        }
    }
}

