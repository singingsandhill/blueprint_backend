package com.chapter1.blueprint.member.controller;

import com.chapter1.blueprint.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //@GetMapping(value = "/members/new")
    //public String createForm() {
    //    return "members/createMemberForm";
    //}
}
