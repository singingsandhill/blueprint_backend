package com.chapter1.blueprint;

import com.chapter1.blueprint.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    /*
    * 컨테이너가 관리하는 엔티티 매니저를 빈에 주입합니다.
    * 이를 통해 개발자가 직접 엔티티 매니저를 생성하고 관리할 필요 없이 컨테이너에 위임할 수 있습니다.
    * */
    @PersistenceContext
    private EntityManager em;

    public Long save(Member member) {
        em.persist(member);
        // 커맨드와 쿼리를 분리
        return member.getId();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
