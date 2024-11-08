package com.chapter1.blueprint.subscription.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter @Setter
@Table(name = "real_estate_price",catalog = "subscription")
public class RealEstatePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "region")
    private String region;

    @Column(name = "ssg_cd")
    private Integer ssgCd;

    @Column(name = "ssg_cd_nm")
    private String ssgCdNm;

    @Column(name = "umd_nm")
    private String umdNm;

    @Column(name = "jibun")
    private String jibun;


    @Column(name = "apt_dong")
    private String aptDong;

    @Column(name = "apt_nm")
    private String aptNm;

    @Column(name = "deal_day")
    private Integer dealDay;

    @Column(name = "deal_month")
    private Integer dealMonth;

    @Column(name = "deal_year")
    private Integer dealYear;

    @Column(name = "deal_date")
    private Date dealDate;

    @Column(name = "deal_amount")
    private Long dealAmount;

    @Column(name = "exclu_use_ar")
    private BigDecimal excluUseAr;
}
