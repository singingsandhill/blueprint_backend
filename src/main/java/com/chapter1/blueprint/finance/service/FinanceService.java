package com.chapter1.blueprint.finance.service;

import com.chapter1.blueprint.finance.repository.ProductListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FinanceService {
    private final ProductListRepository productListRepository;
}
