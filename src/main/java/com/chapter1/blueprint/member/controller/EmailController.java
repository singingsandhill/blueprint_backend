package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.dto.EmailDTO;
import com.chapter1.blueprint.member.dto.MemberDTO;
import com.chapter1.blueprint.member.service.EmailService;
import com.chapter1.blueprint.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("member/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/sendVerification")
    public ResponseEntity<SuccessResponse> sendVerification(@RequestBody EmailDTO emailDTO) {
        emailService.sendVerificationEmail(emailDTO.getEmail());
        return ResponseEntity.ok(new SuccessResponse("인증 코드가 발송되었습니다."));
    }

    @PostMapping("/verifyEmailCode")
    public ResponseEntity<Boolean> verifyEmailCode(@RequestBody EmailDTO emailDTO) {
        boolean isValidCode = emailService.verifyEmailCode(emailDTO.getEmail(), emailDTO.getCode());
        return ResponseEntity.ok(isValidCode);
    }
}

