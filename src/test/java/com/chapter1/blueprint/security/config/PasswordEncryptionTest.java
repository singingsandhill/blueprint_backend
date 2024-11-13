package com.chapter1.blueprint.security.config;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PasswordEncryptionTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testEncryptAndSavePassword() {
        String memberId = "tester01";

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        String rawPassword = "1234*";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        member.setPassword(encodedPassword);
        member.setExpiration(new Timestamp(System.currentTimeMillis()));

        memberRepository.save(member);

        Member updatedMember = memberRepository.findByMemberId(memberId).get();
        assertTrue(passwordEncoder.matches(rawPassword, updatedMember.getPassword()));
    }
}
