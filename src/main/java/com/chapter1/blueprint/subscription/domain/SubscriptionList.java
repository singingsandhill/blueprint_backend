package com.chapter1.blueprint.subscription.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter @Setter
@Table(name = "subscription_list",catalog = "subscription")
public class SubscriptionList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "house_manage_no")
    private Integer houseManageNo;

    @Column(name = "rent_secd")
    private String rentSecd;

    @Column(name = "house_dtl_secd")
    private String houseDtlSecd;

    @Column(name = "rcept_bgnde")
    private Date rceptBgnde;

    @Column(name = "rcept_endde")
    private Date rceptEndde;

    @Column(name = "pblanc_url")
    private String pblancUrl;

}
