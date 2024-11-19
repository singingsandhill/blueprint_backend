package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyDetailFilterRepository extends JpaRepository<PolicyDetailFilter, Long> {
    @Query("SELECT p FROM PolicyDetailFilter p WHERE p.region = :region AND (p.minAge IS NULL OR :age >= p.minAge) AND (p.maxAge IS NULL OR :age <= p.maxAge) AND (p.job IS NULL OR p.job = :job)")
    List<PolicyDetailFilter> findRecommendedPoliciesByUid(@Param("region") String region,
                                                          @Param("age") Integer age,
                                                          @Param("job") String job);
}
