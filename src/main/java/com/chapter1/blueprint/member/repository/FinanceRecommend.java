package com.chapter1.blueprint.member.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FinanceRecommend {
    // 생성자 injection
    private final EntityManager em;
}
