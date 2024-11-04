package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
import com.chapter1.blueprint.member.dto.EmailDTO;
import com.chapter1.blueprint.member.dto.MemberDTO;
import com.chapter1.blueprint.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@RequestBody MemberDTO memberDTO) {
        Map<String, String> tokens = memberService.register(memberDTO);
        return ResponseEntity.ok(new SuccessResponse(tokens));
    }

    @GetMapping("/checkMemberId")
    public ResponseEntity<?> checkMemberId(@RequestParam String memberId) {
        boolean isAvailable = !memberService.checkMemberId(memberId);
        return ResponseEntity.ok().body(isAvailable);
    }

    @GetMapping("/checkEmail")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = !memberService.checkEmailDuplicate(email);
        return ResponseEntity.ok().body(isDuplicate);
    }

    @PostMapping("/profile/{memberId}")
    public ResponseEntity<SuccessResponse> updateProfile(@PathVariable String memberId, @RequestBody InputProfileDTO inputProfileDTO) {
        memberService.updateMemberProfile(memberId, inputProfileDTO);
        return ResponseEntity.ok(new SuccessResponse("업데이트 성공"));
    }

     //@GetMapping(value = "/members/new")
    //public String createForm() {
    //    return "members/createMemberForm";
    //}
}
