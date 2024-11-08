package com.chapter1.blueprint.subscription.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "ssgcode",catalog = "subscription")
public class Ssgcode {
    @Id
    @Column(name = "ssg_cd")
    private Integer ssgCd;

    @Column(name = "ssg_cd_nm")
    private String ssgCdNm;

    @Column(name = "ssg_cd_5")
    private String ssgCd5;

    @Column(name = "ssg_cd_nm_region")
    private String ssgCdNmRegion;

    @Column(name = "ssg_cd_nm_city")
    private String ssgCdNmCity;
}
