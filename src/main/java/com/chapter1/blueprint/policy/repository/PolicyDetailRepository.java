package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyDetailRepository extends JpaRepository<PolicyDetail, Long> {

//    @Query("SELECT new com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO(" +
//            "d.idx, d.subject, d.condition, d.content, d.scale, d.enquiry, d.document, d.url, d.way, " +
//            "f.minAge, f.maxAge, f.job) " +
//            "FROM PolicyDetail d JOIN PolicyDetailFilter f ON d.idx = f.idx " +
//            "WHERE d.idx = :idx")
//    PolicyDetailDTO findPolicyDetailByIdx(@Param("idx") Long idx);

//    PolicyDetail findById(long id);

}
