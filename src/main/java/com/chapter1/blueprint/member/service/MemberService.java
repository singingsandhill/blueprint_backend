package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 회원 가입
    public String join(Member member) {
        memberRepository.save(member);
        return member.getId();
    }

}
