package com.chapter1.blueprint.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter @Setter
public class RealEstatePrice {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;

    @Column(name = "region")
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "detail")
    private String detail;

    @Column(name = "name")
    private String name;

    @Column(name = "date")
    private Date date;

    @Column(name = "price")
    private Long price;

    @Column(name = "diff_price")
    private Long diffPrice;

    @Column(name = "diff_ratio")
    private BigDecimal diffRatio;

}
