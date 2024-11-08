package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.DTO.RealEstatePriceSummaryDTO;
import com.chapter1.blueprint.subscription.domain.RealEstatePriceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealEstatePriceSummaryReapository extends JpaRepository<RealEstatePriceSummary, Integer> {

    @Query(value= "SELECT new RealEstatePriceSummaryDTO(" +
            "p.rigeon, p.sggCdNm, p.umdNm, p.dealYear, p.dealMonth, p.dealCount, p.pricePerAr) " +
            "FROM RealEstatePriceSummary p " +
            "WHERE p.rigeon = :rigeon AND p.sggCdNm = :sggCdNm AND p.umdNm = :umdNm")
    List<RealEstatePriceSummaryDTO> findByRigeonAndSggCdNmAndUmdNm(
            @Param("rigeon") String rigeon,
            @Param("sggCdNm") String sggCdNm,
            @Param("umdNm") String umdNm);



}
