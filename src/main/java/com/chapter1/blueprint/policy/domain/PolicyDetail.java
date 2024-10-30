package com.chapter1.blueprint.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class PolicyDetail {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;

    @Column(name = "subject")
    private String subject;

    @Column(name = "condition")
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
