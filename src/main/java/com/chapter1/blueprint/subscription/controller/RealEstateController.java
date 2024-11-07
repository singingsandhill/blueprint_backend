package com.chapter1.blueprint.subscription.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.subscription.service.RealEstateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/realestate")
public class RealEstateController {
    private final RealEstateService realEstateService;

    @GetMapping(value = "/get")
    public ResponseEntity<?> getRealEstatePrice() {
        String result = realEstateService.getRealEstatePrice();
        return ResponseEntity.ok(new SuccessResponse(result));
    }
}
