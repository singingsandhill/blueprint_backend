package com.chapter1.blueprint.finance.repository;

import com.chapter1.blueprint.finance.domain.LoanList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface LoanListRepository extends JpaRepository<LoanList, Long> {

    @Query(value = "SELECT * FROM finance.loan_list ORDER BY lend_rate_avg LIMIT 1", nativeQuery = true)
    LoanList getLoanFilter();

    // @Query("SELECT l FROM LoanList l WHERE (:filter1 IS NULL OR l.mrtg_type_nm = :filter1) AND (:filter2 IS NULL OR l.lend_rate_type_nm = :filter2)")
    @Query("SELECT l FROM LoanList l WHERE (:mrtgTypeNm is null or l.mrtgTypeNm = :mrtgTypeNm) AND (:lendRateTypeNm is null or l.lendRateTypeNm = :lendRateTypeNm)")
    Page<LoanList> findLoansWithFilters(@Param("mrtgTypeNm") String mrtgTypeNm, @Param("lendRateTypeNm") String lendRateTypeNm, Pageable pageable);

}
