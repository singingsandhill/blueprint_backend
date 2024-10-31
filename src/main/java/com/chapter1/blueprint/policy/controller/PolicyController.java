package com.chapter1.blueprint.policy.controller;

import com.chapter1.blueprint.global.DTO.ResponseDTO;
import com.chapter1.blueprint.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping(value = "/policy/update/TK")
    public ResponseEntity<?> updatePolicyTK() {
        String result = policyService.updatePolicyTK();
        return ResponseEntity.ok(new ResponseDTO(true,result));
    }
}
