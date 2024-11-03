package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
import com.chapter1.blueprint.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/profile/{uid}")
    public ResponseEntity<String> updateProfile(@PathVariable Long uid, @RequestBody InputProfileDTO inputProfileDTO) {
        memberService.updateMemberProfile(uid, inputProfileDTO);
        return ResponseEntity.ok("업데이트 성공");
    }

    //@GetMapping(value = "/members/new")
    //public String createForm() {
    //    return "members/createMemberForm";
    //}
}
