package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
import com.chapter1.blueprint.member.domain.dto.FindPasswordDTO;
import com.chapter1.blueprint.member.domain.dto.PasswordDTO;
import com.chapter1.blueprint.member.domain.dto.ProfileInfoDTO;
import com.chapter1.blueprint.member.dto.MemberDTO;
import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import com.chapter1.blueprint.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final PolicyAlarmRepository policyAlarmRepository;
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@RequestBody MemberDTO memberDTO) {
        Map<String, String> tokens = memberService.register(memberDTO);
        return ResponseEntity.ok(new SuccessResponse(tokens));
    }

    @GetMapping("/checkMemberId")
    public ResponseEntity<?> checkMemberId(@RequestParam String memberId) {
        boolean isAvailable = memberService.checkMemberId(memberId);
        logger.info("Final response for memberId check: {}", isAvailable);

        return ResponseEntity.ok().body(isAvailable);
    }

    @GetMapping("/checkEmail")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = !memberService.checkEmailDuplicate(email);
        return ResponseEntity.ok().body(isDuplicate);
    }

    @PostMapping("/find/memberId")
    public ResponseEntity<SuccessResponse> findMemberId(@RequestBody MemberDTO memberDTO) {
        logger.info("아이디 찾기 요청: 이름 - {}, 이메일 - {}", memberDTO.getMemberName(), memberDTO.getEmail());
        String memberId = memberService.findByMemberNameAndEmail(memberDTO.getMemberName(), memberDTO.getEmail());
        return ResponseEntity.ok(new SuccessResponse(memberId));
    }

    @PostMapping("/find/password")
    public ResponseEntity<SuccessResponse> findPassword(@RequestBody FindPasswordDTO findPasswordDTO) {
        logger.info("비밀번호 찾기 요청: 아이디 - {}, 이메일 - {}", findPasswordDTO.getMemberId(), findPasswordDTO.getEmail());
        String temporaryPassword = memberService.generateTemporaryPassword(findPasswordDTO.getMemberId(), findPasswordDTO.getEmail());
        return ResponseEntity.ok(new SuccessResponse("임시 비밀번호가 이메일로 발송되었습니다."));
    }

    @GetMapping("/mypage")
    public ResponseEntity<SuccessResponse> getInfoMyPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedMemberId = authentication.getName();

        ProfileInfoDTO profileInfoDTO = memberService.getInfoProfile(authenticatedMemberId);
        return ResponseEntity.ok(new SuccessResponse(profileInfoDTO));
    }

    @PostMapping("/mypage")
    public ResponseEntity<SuccessResponse> updateMyPage(@RequestBody InputProfileDTO inputProfileDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedMemberId = authentication.getName();

        memberService.updateMemberProfile(authenticatedMemberId, inputProfileDTO);
        return ResponseEntity.ok(new SuccessResponse("업데이트 성공"));
    }

    @PostMapping("/verification/password")
    public ResponseEntity<SuccessResponse> verifyPassword(@RequestBody PasswordDTO passwordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedMemberId = authentication.getName();

        boolean result = memberService.verifyPassword(authenticatedMemberId, passwordDTO);
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @PostMapping("/change/password")
    public ResponseEntity<SuccessResponse> updatePassword(@RequestBody PasswordDTO passwordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedMemberId = authentication.getName();

        memberService.updatePassword(authenticatedMemberId, passwordDTO);
        return ResponseEntity.ok(new SuccessResponse("비밀번호 변경 성공"));
    }

    @PutMapping("/notification/{uid}/{policyIdx}")
    public ResponseEntity<String> updateNotificationSettings(@PathVariable Long uid,
                                                             @PathVariable Long policyIdx,
                                                             @RequestBody PolicyAlarm updatedSettings) {

        PolicyAlarm currentSettings = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);

        if (currentSettings == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification settings not found.");
        }

        currentSettings.setNotificationEnabled(updatedSettings.getNotificationEnabled());

        policyAlarmRepository.save(currentSettings);

        return ResponseEntity.ok("Notification settings updated successfully.");
    }

    @DeleteMapping("/notification/{uid}/{policyIdx}")
    public ResponseEntity<String> deleteNotificationSettings(@PathVariable Long uid,
                                                             @PathVariable Long policyIdx) {

        PolicyAlarm currentSettings = policyAlarmRepository.findByUidAndPolicyIdx(uid, policyIdx);

        if (currentSettings == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification settings not found.");
        }

        policyAlarmRepository.delete(currentSettings);
        return ResponseEntity.ok("Notification settings deleted successfully.");
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedMemberId = authentication.getName();

        Long uid = memberService.getUidByMemberId(authenticatedMemberId);

        List<PolicyAlarm> notifications = memberService.getNotificationsByUid(uid);
        return ResponseEntity.ok(new SuccessResponse(notifications));
    }




    //@GetMapping(value = "/members/new")
    //public String createForm() {
    //    return "members/createMemberForm";
    //}
}
