package com.chapter1.blueprint.repository;

import com.chapter1.blueprint.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    // 생성자 injection
    private final EntityManager em;

    // 저장
    public Long save(Member member) {
        em.persist(member);
        // 커맨드와 쿼리를 분리
        return member.getId();
    }

    // 조회
    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    // 다수 조회
    public List<Member> findAll() {
        //JPA query
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    // 이름으로 조회
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name).getResultList();
    }
}
