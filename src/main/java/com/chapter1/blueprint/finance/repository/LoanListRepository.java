package com.chapter1.blueprint.finance.repository;

import com.chapter1.blueprint.finance.domain.LoanList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanListRepository extends JpaRepository<LoanList, Long> {
}
