package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.RealEstatePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealEstatePriceRepository extends JpaRepository<RealEstatePrice, Long> {

    @Query("SELECT DISTINCT city FROM RealEstatePrice")
    List<String> getCityList();

    @Query("SELECT DISTINCT district FROM RealEstatePrice WHERE city = :city")
    List<String> getDistrict(@Param("city") String city);

    @Query("SELECT DISTINCT local FROM RealEstatePrice WHERE city = :city AND district = :district")
    List<String> getLocal(@Param("city") String city, @Param("district") String district);
}
