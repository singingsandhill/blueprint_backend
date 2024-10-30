package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.finance.repository.ProductListRepository;
import com.chapter1.blueprint.policy.repository.PolicyDetailRepositpry;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyListRepository policyListRepository;
    private final PolicyDetailRepositpry policyDetailRepositpry;
}
