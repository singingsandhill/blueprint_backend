package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
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

    // 마이페이지에서 추가 사항 입력 시 db 업데이트 (db변경 시 자동으로 update 해줌)
    // 그래서 save 메소드 필요없음
    public void updateMemberProfile(Long uid, InputProfileDTO inputProfileDTO) {
        Member memberProfile = memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + uid));

        memberProfile.setIncome(inputProfileDTO.getIncome());
        memberProfile.setOccupation(inputProfileDTO.getOccupation());
        memberProfile.setResidence(inputProfileDTO.getResidence());
        memberProfile.setMaritalStatus(inputProfileDTO.getMaritalStatus());
        memberProfile.setHasChildren(inputProfileDTO.getHasChildren());
        memberProfile.setHousingType(inputProfileDTO.getHousingType());

    }

}
