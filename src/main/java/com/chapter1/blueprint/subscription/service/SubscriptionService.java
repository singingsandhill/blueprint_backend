package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import com.chapter1.blueprint.subscription.repository.SubscriptionListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final RealEstatePriceRepository realEstatePriceRepository;
    private final SubscriptionListRepository subscriptionListRepository;
}
