package com.chapter1.blueprint.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter @Setter
@Table(name = "loan_list",catalog = "finance")
public class LoanList {
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

    @Column(name = "loan_lmt")
    private String loanLmt;

    @Column(name = "mrtg_type_nm")
    private String mrtgTypeNm;

    @Column(name = "lend_rate_type_nm")
    private String lendRateTypeNm ;

    @Column(name = "rpay_type_nm")
    private String rpayTypeNm ;

    @Column(name = "lend_rate_min")
    private BigDecimal lendRateMin ;

    @Column(name = "lend_rate_max")
    private BigDecimal lendRateMax ;

    @Column(name = "lend_rate_avg")
    private BigDecimal lendRateAvg ;

    @Column(name = "prd_category")
    private String prdCategory;

    @Column(name = "image_url")
    private String imageUrl;
}
