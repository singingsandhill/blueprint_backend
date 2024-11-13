package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.RealEstatePriceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT s.region FROM RealEstatePriceSummary s ORDER BY s.region")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT s.sggCdNm FROM RealEstatePriceSummary s WHERE s.region = :region ORDER BY s.sggCdNm")
    List<String> findDistinctSggCdNmByRegion(@Param("region") String region);

    @Query("SELECT DISTINCT s.umdNm FROM RealEstatePriceSummary s WHERE s.region = :region AND s.sggCdNm = :sggCdNm ORDER BY s.umdNm")
    List<String> findDistinctUmdNmByRegionAndSggCdNm(
            @Param("region") String region,
            @Param("sggCdNm") String sggCdNm
    );


}
