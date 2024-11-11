package com.chapter1.blueprint.subscription.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter @Setter
@Table(name = "real_estate_price", catalog = "subscription")
public class RealEstatePrice {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;

    @Column(name = "region")
    private String city;

    @Column(name = "ssg_cd")
    private String ssgCd;

    @Column(name = "ssg_cd_nm")
    private String district;

    @Column(name = "umd_nm")
    private String local;

    @Column(name = "jibun")
    private String jibun;

    @Column(name = "apt_nm")
    private Date aptNm;

    @Column(name = "deal_day")
    private Long dealDay;

    @Column(name = "deal_month")
    private Long dealMonth;

    @Column(name = "deal_year")
    private BigDecimal dealYear;

    @Column(name = "deal_date")
    private BigDecimal dealDate;

    @Column(name = "deal_amount")
    private BigDecimal dealAmount;

    @Column(name = "exclu_use_ar")
    private BigDecimal excluUseAr;

}
