package com.chapter1.blueprint.finance.repository;

import com.chapter1.blueprint.finance.domain.LoanList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanListRepository extends JpaRepository<LoanList, Long> {

    @Query(value = "SELECT * FROM finance.loan_list ORDER BY lend_rate_avg LIMIT 1", nativeQuery = true)
    LoanList getLoanFilter();
}
