package com.chapter1.blueprint.policy.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.service.MemberService;
import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.domain.dto.FilterDTO;
import com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO;
import com.chapter1.blueprint.policy.domain.dto.PolicyListDTO;
import com.chapter1.blueprint.policy.service.PolicyDetailService;
import com.chapter1.blueprint.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/policy")
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyDetailService policyDetailService;
    private final MemberService memberService;

    @GetMapping(value = "/update/TK")
    public ResponseEntity<?> updatePolicyTK() {
        String result = policyService.updatePolicyTK();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @GetMapping(value = "/update/company")
    public ResponseEntity<?> updatePolicyCompany() {
        String result = policyService.updatePolicyCompany();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @GetMapping("/list")
    public ResponseEntity<SuccessResponse> getPolicyList() {
        List<PolicyListDTO> policyList = policyDetailService.getPolicyList();
        return ResponseEntity.ok(new SuccessResponse(policyList));
    }

//    @GetMapping("/detail/{idx}")
//    public ResponseEntity<SuccessResponse> getPolicyDetail(@PathVariable Long idx) {
//        PolicyDetailDTO policyDetailDTO = policyDetailService.getPolicyDetail(idx);
//        return ResponseEntity.ok(new SuccessResponse(policyDetailDTO));
//    }

    @GetMapping("/detail/{idx}")
    public ResponseEntity<SuccessResponse> getPolicyDetail(@PathVariable Long idx) {
        PolicyDetail policyDetail = policyDetailService.getPolicyDetail(idx);
        return ResponseEntity.ok(new SuccessResponse(policyDetail));
    }

    @PostMapping("/filter")
    public ResponseEntity<SuccessResponse> getPolicyListByFiltering(@RequestBody FilterDTO filterDTO) {
        List<PolicyList> policyListByFiltering = policyDetailService.getPolicyListByFiltering(filterDTO);
        return ResponseEntity.ok(new SuccessResponse(policyListByFiltering));
    }

    @GetMapping("/deadline")
    public ResponseEntity<SuccessResponse> checkPolicyDeadline() {
        List<PolicyListDTO> policies = policyService.findPoliciesWithApproachingDeadline();
        return ResponseEntity.ok(new SuccessResponse(policies));
    }

    @PostMapping("/manual-check-deadline")
    public ResponseEntity<String> manualCheckPolicyDeadline() {
        checkPolicyDeadline();
        return ResponseEntity.ok("Policy deadline check triggered manually.");
    }

    @GetMapping("/recommendation")
    public ResponseEntity<SuccessResponse> recommendPolicy() {
        Long uid = memberService.getAuthenticatedUid();

        List<PolicyList> recommendedPolicy = policyDetailService.recommendPolicy(uid);
        return ResponseEntity.ok(new SuccessResponse(recommendedPolicy));
    }

    @GetMapping("/peer")
    public ResponseEntity<SuccessResponse> getPeerPolicy() {
        Long uid = memberService.getAuthenticatedUid();

        List<PolicyList> peerPolicy = policyDetailService.getPeerPolicy(uid);
        return ResponseEntity.ok(new SuccessResponse(peerPolicy));
    }
}
