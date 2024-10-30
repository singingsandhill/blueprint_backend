package com.chapter1.blueprint.subscription.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscriptionListRepository {
    private final EntityManager em;
}
