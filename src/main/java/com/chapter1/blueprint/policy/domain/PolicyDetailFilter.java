package com.chapter1.blueprint.policy.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "policy_detail_filter", catalog = "policy")
public class PolicyDetailFilter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "target")
    private String target;

    @Column(name = "condition")
    private String condition;

    @Column(name = "content")
    private String content;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_Age")
    private Integer maxAge;

    @Column(name = "region")
    private String region;

    @Column(name = "job")
    private String job;
}
