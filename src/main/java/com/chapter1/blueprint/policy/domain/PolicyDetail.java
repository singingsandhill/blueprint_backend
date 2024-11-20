package com.chapter1.blueprint.policy.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "policy_detail",catalog = "policy")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PolicyDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "subject")
    private String subject;

    @Column(name = "`condition`")
    private String condition;

    @Column(name = "content")
    private String content;

    @Column(name = "scale")
    private String scale;

    @Column(name = "enquiry")
    private String enquiry;

    @Column(name = "way")
    private String way;

    @Column(name = "document")
    private String document;

    @Column(name = "url")
    private String url;

    @Column(name = "target")
    private String target;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "job")
    private String job;

    @Column(name = "income")
    private String income;

    @Column(name = "exclusion")
    private String exclusion;

    @Column(name = "application_site")
    private String applicationSite;

    @Column(name = "location")
    private String location;
}
