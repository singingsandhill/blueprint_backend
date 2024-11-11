package com.chapter1.blueprint.finance.repository;

import com.chapter1.blueprint.finance.domain.LoanList;
import com.chapter1.blueprint.finance.domain.SavingsList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsListRepository extends JpaRepository<SavingsList, Long> {

    @Query(value = "SELECT * FROM finance.savings_list ORDER BY intr_rate2 DESC LIMIT 1", nativeQuery = true)
    SavingsList getSavingsFilter();

}
