package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyDetailFilterRepository extends JpaRepository<PolicyDetailFilter, Long> {
}
