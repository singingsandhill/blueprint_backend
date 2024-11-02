package com.chapter1.blueprint.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chapter1.blueprint.policy.domain.PolicyList;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyListRepository extends JpaRepository<PolicyList,String> {

}
