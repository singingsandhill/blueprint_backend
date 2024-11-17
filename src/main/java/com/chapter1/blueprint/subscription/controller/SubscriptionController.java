package com.chapter1.blueprint.subscription.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.subscription.domain.SubscriptionList;
import com.chapter1.blueprint.subscription.domain.DTO.ResidenceDTO;
import com.chapter1.blueprint.subscription.service.ResidenceService;
import com.chapter1.blueprint.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final ResidenceService residenceService;

    @GetMapping(value = "/update")
    public ResponseEntity<?> updateSubscription() {
        String result = subscriptionService.updateSubAPT()+subscriptionService.updateSubAPT2()+subscriptionService.updateSubOther();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @GetMapping(value = "/get")
    public ResponseEntity<?> getSubscription(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SubscriptionList> subscriptionLists = subscriptionService.getAllSubscription(PageRequest.of(page, size));
        return ResponseEntity.ok(new SuccessResponse(subscriptionLists));
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllSubscriptions() {
        List<SubscriptionList> subscriptionLists = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(new SuccessResponse(subscriptionLists));
    }

    @GetMapping("/city")
    public ResponseEntity<SuccessResponse> getCityList() {
        List<String> cityList = residenceService.getCityList();
        return ResponseEntity.ok(new SuccessResponse(cityList));
    }

    @PostMapping("/district")
    public ResponseEntity<SuccessResponse> getDistrict(@RequestBody ResidenceDTO residenceDTO) {
        List<String> districtList = residenceService.getDistrict(residenceDTO);
        return ResponseEntity.ok(new SuccessResponse(districtList));
    }

    @PostMapping("/local")
    public ResponseEntity<SuccessResponse> getLocal(@RequestBody ResidenceDTO residenceDTO) {
        List<String> localList = residenceService.getLocal(residenceDTO);
        return ResponseEntity.ok(new SuccessResponse(localList));
    }

    @GetMapping("/recommendation")
    public ResponseEntity<SuccessResponse> recommendSubscription() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedMemberId = authentication.getName();

        List<SubscriptionList> recommendedSubscription = subscriptionService.recommendSubscription(authenticatedMemberId);
        return ResponseEntity.ok(new SuccessResponse(recommendedSubscription));
    }

}
