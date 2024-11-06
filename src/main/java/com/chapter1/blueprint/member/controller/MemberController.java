package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
import com.chapter1.blueprint.member.domain.dto.ProfileInfoDTO;
import com.chapter1.blueprint.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile/{memberId}")
    public ResponseEntity<SuccessResponse> getInfoProfile(@PathVariable String memberId) {
        ProfileInfoDTO profileInfoDTO = memberService.getInfoProfile(memberId);
        return ResponseEntity.ok(new SuccessResponse(profileInfoDTO));
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
