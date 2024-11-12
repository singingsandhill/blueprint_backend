package com.chapter1.blueprint.subscription.domain.DTO;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class RealEstateDTO {
    private Long idx;

    private String region;

    private Integer sggCd;

    private String sggCdNm;

    private String umdNm;

    private String jibun;

    private String name;

    private String aptDong;

    private String aptNm;

    private Integer dealDay;

    private Integer dealMonth;

    private Integer dealYear;

    private Long dealAmount;

    private BigDecimal excluUseAr;
}
