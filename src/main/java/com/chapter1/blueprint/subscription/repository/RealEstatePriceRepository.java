package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.RealEstatePrice;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealEstatePriceRepository extends JpaRepository<RealEstatePrice, Long> {

    @Modifying
    @Query(value = "UPDATE subscription.real_estate_price p " +
            "JOIN subscription.ssgcode s ON p.ssg_cd = s.ssg_cd_5 " +
            "SET p.region = s.ssg_cd_nm_region, p.ssg_cd_nm = s.ssg_cd_nm_city " +
            "WHERE p.ssg_cd_nm IS NULL", nativeQuery = true)
    int updateRealEstatePriceFromSsgcode();

    @Modifying
    @Query(value = "INSERT INTO subscription.real_estate_price_summary (region, sgg_cd_nm, umd_nm, deal_year, deal_month, deal_count, price_per_ar) " +
            "SELECT region, ssg_cd_nm, umd_nm, deal_year, deal_month, COUNT(*) AS deal_count, " +
            "AVG(deal_amount / exclu_use_ar) AS price_per_ar " +
            "FROM subscription.real_estate_price " +
            "GROUP BY ssg_cd, umd_nm, deal_year, deal_month " +
            "ORDER BY ssg_cd, umd_nm, deal_month", nativeQuery = true)
    void insertSummary();

    @Query("SELECT DISTINCT region FROM RealEstatePrice")
    List<String> getCityList();

    @Query("SELECT DISTINCT ssgCdNm FROM RealEstatePrice WHERE region = :city ORDER BY ssgCdNm")
    List<String> getDistrict(@Param("city") String city);

    @Query("SELECT DISTINCT umdNm FROM RealEstatePrice WHERE region = :city AND ssgCdNm = :district ORDER BY umdNm")
    List<String> getLocal(@Param("city") String city, @Param("district") String district);

}
