package com.chapter1.blueprint.service;

import com.chapter1.blueprint.domain.Member;
import com.chapter1.blueprint.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
