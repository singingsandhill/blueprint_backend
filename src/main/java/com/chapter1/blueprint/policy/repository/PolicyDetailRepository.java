package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyDetailRepository extends JpaRepository<PolicyDetail, Long> {

}
