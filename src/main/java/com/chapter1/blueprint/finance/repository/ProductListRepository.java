package com.chapter1.blueprint.finance.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductListRepository {
    // 생성자 injection
    private final EntityManager em;
}
