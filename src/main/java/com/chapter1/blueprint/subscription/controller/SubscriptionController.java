package com.chapter1.blueprint.subscription.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.subscription.domain.SubscriptionList;
import com.chapter1.blueprint.subscription.domain.dto.ResidenceDTO;
import com.chapter1.blueprint.subscription.service.ResidenceService;
import com.chapter1.blueprint.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        String result = subscriptionService.updateSub();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @GetMapping(value = "/get")
            public ResponseEntity<?> getSubscription() {
        List<SubscriptionList> subscriptionLists = subscriptionService.getAllSubscription();
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

}
