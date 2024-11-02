package com.chapter1.blueprint.policy.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter @Setter
@Table(name = "policy_list",catalog = "policy")
public class PolicyList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "offer_inst")
    private String offerInst;

    @Column(name = "manage_inst")
    private String manageInst;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "apply_start_date")
    private Date applyStartDate;

    @Column(name = "apply_end_date")
    private Date applyEndDate;
}
