package com.chapter1.blueprint.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SubscriptionList {
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

}
