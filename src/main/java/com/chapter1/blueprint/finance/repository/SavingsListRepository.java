package com.chapter1.blueprint.finance.repository;

import com.chapter1.blueprint.finance.domain.LoanList;
import com.chapter1.blueprint.finance.domain.SavingsList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsListRepository extends JpaRepository<SavingsList, Long> {

    @Query(value = "SELECT * FROM finance.savings_list ORDER BY intr_rate2 DESC LIMIT 1", nativeQuery = true)
    SavingsList getSavingsFilter();

    @Query("SELECT s FROM SavingsList s WHERE (:intrRateNm is null or s.intrRateNm like :intrRateNm) AND (:prdCategory is null or s.prdCategory = :prdCategory)")
    Page<SavingsList> findSavingsWithFilters(@Param("intrRateNm") String intrRateNm, @Param("prdCategory") String prdCategory, Pageable pageable);

}
