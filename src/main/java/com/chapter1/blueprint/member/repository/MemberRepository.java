package com.chapter1.blueprint.member.repository;

import com.chapter1.blueprint.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByMemberNameAndEmail(String memberName, String email);
    Optional<Member> findByMemberIdAndEmail(String memberId, String email);

    boolean existsByMemberId(String memberId);
    boolean existsByEmail(String email);

    @Query("SELECT m.password FROM Member m WHERE m.memberId = :memberId")
    String findPasswordByMemberId(@Param("memberId") String memberId);

}
