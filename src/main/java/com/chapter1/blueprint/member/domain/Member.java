package com.chapter1.blueprint.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "uid")
    private Long uid;

    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 100)
    private String membername;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 10)
    private String social;

    @Column(name = "birth_year", nullable = false)
    private Integer birthYear;

    @Column(length = 20, nullable = false)
    private String birth;

    @Column(length = 10)
    private String gender;

    @Column(length = 255)
    private String profile;

    @Column(name = "agreement_info", nullable = false)
    private Boolean agreementInfo;

    @Column(name = "agreement_finance", nullable = false)
    private Boolean agreementFinance;

    @Column(length = 20)
    private String auth;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;
}
