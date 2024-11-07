package com.chapter1.blueprint.member.service;
import com.chapter1.blueprint.exception.codes.ErrorCode;
import com.chapter1.blueprint.exception.codes.ErrorCodeException;
import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.domain.dto.InputProfileDTO;
import com.chapter1.blueprint.member.domain.dto.PasswordDTO;
import com.chapter1.blueprint.member.domain.dto.ProfileInfoDTO;
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
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProcessor jwtProcessor;
    private final EmailService emailService;
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
        member.setAgreementService(memberDTO.getAgreementService());
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

    public String findByMemberNameAndEmail(String memberName, String email) {
        return memberRepository.findByMemberNameAndEmail(memberName, email)
                .map(Member::getMemberId)
                .orElseThrow(() -> new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public String generateTemporaryPassword(String memberId, String email) {
        Optional<Member> memberOptional = memberRepository.findByMemberIdAndEmail(memberId, email);
        if (memberOptional.isEmpty()) {
            throw new ErrorCodeException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Member member = memberOptional.get();

        String temporaryPassword = createRandomPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        member.setPassword(encodedPassword);
        memberRepository.save(member);

        try {
            emailService.sendTemporaryPassword(email, temporaryPassword);
        } catch (Exception e) {
            throw new ErrorCodeException(ErrorCode.EMAIL_SENDING_FAILED);
        }

        return temporaryPassword;
    }

    private String createRandomPassword() {
        int length = 10;
        String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characterSet.length());
            password.append(characterSet.charAt(index));
        }

        return password.toString();
    }

    // 마이페이지에서 추가 사항 입력 시 db 업데이트 (db변경 시 자동으로 update 해줌)
    public void updateMemberProfile(String memberId, InputProfileDTO inputProfileDTO) {
        Member memberProfile = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID (InputProfile): " + memberId));

        memberProfile.setIncome(inputProfileDTO.getIncome());
        memberProfile.setOccupation(inputProfileDTO.getOccupation());
        memberProfile.setResidence(inputProfileDTO.getResidence());
        memberProfile.setMaritalStatus(inputProfileDTO.getMaritalStatus());
        memberProfile.setHasChildren(inputProfileDTO.getHasChildren());
        memberProfile.setHousingType(inputProfileDTO.getHousingType());

        memberRepository.save(memberProfile);
    }

    public ProfileInfoDTO getInfoProfile(String memberId) {
        Member memberProfileInfo = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID (ProfileInfo): " + memberId));

       ProfileInfoDTO profileInfoDTO = new ProfileInfoDTO();
        profileInfoDTO.setPassword(memberProfileInfo.getPassword());
        profileInfoDTO.setEmail(memberProfileInfo.getEmail());
        profileInfoDTO.setIncome(memberProfileInfo.getIncome());
        profileInfoDTO.setOccupation(memberProfileInfo.getOccupation());
        profileInfoDTO.setResidence(memberProfileInfo.getResidence());
        profileInfoDTO.setMaritalStatus(memberProfileInfo.getMaritalStatus());
        profileInfoDTO.setHasChildren(memberProfileInfo.getHasChildren());
        profileInfoDTO.setHousingType(memberProfileInfo.getHousingType());

        return profileInfoDTO;
    }

    public boolean verifyPassword(String memberId, PasswordDTO passwordDTO) {
        String password = memberRepository.findPasswordByMemberId(memberId);
        return passwordEncoder.matches(passwordDTO.getPassword(), password);
    }

}
