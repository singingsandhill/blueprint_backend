package com.chapter1.blueprint.subscription.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping(value = "/update/subscription")
    public ResponseEntity<?> updateSubscription() {
        String result = subscriptionService.updateSub();
        return ResponseEntity.ok(new SuccessResponse(result));
    }
}
