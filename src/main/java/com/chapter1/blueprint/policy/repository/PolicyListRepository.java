package com.chapter1.blueprint.policy.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PolicyListRepository {
    // 생성자 injection
    private final EntityManager em;
}
