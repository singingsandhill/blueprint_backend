package com.chapter1.blueprint.subscription.domain.DTO;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class SubscriptionDTO {
    private Long idx;

    private String region;

    private String city;

    private String district;

    private String detail;

    private String name;

    private Integer houseManageNo;

    private String rentSecd;

    private String houseDtlSecd;

    private Date rceptBgnde;

    private Date rceptEndde;

    private String pblancUrl;
}
