package com.chapter1.blueprint.policy.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO;
import com.chapter1.blueprint.policy.service.PolicyDetailService;
import com.chapter1.blueprint.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/policy")
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyDetailService policyDetailService;

    @GetMapping(value = "/update/TK")
    public ResponseEntity<?> updatePolicyTK() {
        String result = policyService.updatePolicyTK();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @GetMapping("/detail/{idx}")
    public ResponseEntity<SuccessResponse> getPolicyDetail(@PathVariable Long idx) {
        PolicyDetailDTO policyDetailDTO = policyDetailService.getPolicyDetail(idx);
        return ResponseEntity.ok(new SuccessResponse(policyDetailDTO));
    }
}
