package com.chapter1.blueprint.repository;


import com.chapter1.blueprint.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    // 생성자 injection
    private final EntityManager em;

    // 저장
    public String save(Member member) {
        em.persist(member);
        // 커맨드와 쿼리를 분리
        return member.getId();
    }
}
