package com.chapter1.blueprint.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.naming.Name;
import java.math.BigDecimal;

@Entity
@Getter @Setter
@Table(name = "savings_list",catalog = "finance")
public class SavingsList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "fin_prdt_cd")
    private String finPrdtCd;

    @Column(name = "kor_co_nm")
    private String korCoNm;

    @Column(name = "dcls_month")
    private String dclsMonth;

    @Column(name = "fin_prdt_nm")
    private String finPrdtNm;

    @Column(name = "join_way")
    private String joinWay;

    @Column(name = "join_member")
    private String joinMember;

    @Column(name = "intr_rate_type_nm")
    private String intrRateNm;

    @Column(name = "save_trm")
    private Integer saveTrm ;

    @Column(name = "intr_rate")
    private BigDecimal intrRate ;

    @Column(name = "intr_rate2")
    private BigDecimal intrRate2 ;

    @Column(name = "prd_category")
    private String prdCategory;

    @Column(name = "image_url")
    private String imageUrl;
}
