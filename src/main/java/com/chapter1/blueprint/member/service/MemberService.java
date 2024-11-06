package com.chapter1.blueprint.member.service;
import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
import com.chapter1.blueprint.member.dto.MemberDTO;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProcessor jwtProcessor;
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);


    public Map<String, String> register(MemberDTO memberDTO) {
        Member member = new Member();
        member.setMemberId(memberDTO.getMemberId());
        member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        member.setMemberName(memberDTO.getMemberName());
        member.setEmail(memberDTO.getEmail());
        member.setGender(memberDTO.getGender());
        member.setBirthYear(memberDTO.getBirthYear());
        member.setBirth(memberDTO.getBirth());
        member.setAgreementService(memberDTO.getAgreementFinance());
        member.setAgreementInfo(memberDTO.getAgreementInfo());
        member.setAuth("ROLE_MEMBER");

        String refreshToken = jwtProcessor.generateRefreshToken(member.getMemberId());
        Timestamp expiration = jwtProcessor.getRefreshTokenExpiration();

        member.setRefreshToken(refreshToken);
        member.setExpiration(expiration);

        memberRepository.save(member);

        String accessToken = jwtProcessor.generateAccessToken(member.getMemberId(), member.getUid(), member.getAuth(), member.getMemberName(), member.getEmail());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    public boolean checkMemberId(String memberId) {

        logger.info("Received memberId for check: {}", memberId);

        boolean exists = memberRepository.existsByMemberId(memberId);

        logger.info("Exists in DB: {}", exists);
        return exists;
    }
    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 마이페이지에서 추가 사항 입력 시 db 업데이트 (db변경 시 자동으로 update 해줌)
    // 그래서 save 메소드 필요없음
    public void updateMemberProfile(String memberId, InputProfileDTO inputProfileDTO) {
        Member memberProfile = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        memberProfile.setIncome(inputProfileDTO.getIncome());
        memberProfile.setOccupation(inputProfileDTO.getOccupation());
        memberProfile.setResidence(inputProfileDTO.getResidence());
        memberProfile.setMaritalStatus(inputProfileDTO.getMaritalStatus());
        memberProfile.setHasChildren(inputProfileDTO.getHasChildren());
        memberProfile.setHousingType(inputProfileDTO.getHousingType());

        memberRepository.save(memberProfile);

    }

}
