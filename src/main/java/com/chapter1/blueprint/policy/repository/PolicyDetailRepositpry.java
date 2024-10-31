package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyDetailRepositpry extends JpaRepository<PolicyDetail, String> {

}
