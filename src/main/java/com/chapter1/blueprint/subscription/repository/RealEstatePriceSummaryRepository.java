package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.DTO.RealEstatePriceSummaryDTO;
import com.chapter1.blueprint.subscription.domain.RealEstatePriceSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealEstatePriceSummaryRepository extends JpaRepository<RealEstatePriceSummary, Integer> {

    @Query(nativeQuery = true,
            value = "SELECT  region, sgg_cd_nm, umd_nm, deal_year, deal_month, deal_count, price_per_ar " +
                    "FROM subscription.real_estate_price_summary  WHERE region = :region " +
                    "AND sgg_cd_nm = :sggCdNm AND umd_nm = :umdNm",
            name = "RealEstatePriceSummaryDTOMapping")
    List<Object[]> findByRegionAndSggCdNmAndUmdNm(
            @Param("region") String region,
            @Param("sggCdNm") String sggCdNm,
            @Param("umdNm") String umdNm);
}
