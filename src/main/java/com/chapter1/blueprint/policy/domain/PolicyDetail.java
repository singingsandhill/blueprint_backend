package com.chapter1.blueprint.policy.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "policy_detail",catalog = "policy")
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
}
