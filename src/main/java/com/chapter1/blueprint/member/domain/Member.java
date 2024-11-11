package com.chapter1.blueprint.member.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "member_info", catalog = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private Long uid;

    @Column(name = "member_id", length = 50, nullable = false)
    private String memberId;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "member_name", length = 100)
    private String memberName;

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
    private Boolean agreementInfo = false;

    @Column(name = "agreement_service", nullable = false)
    private Boolean agreementService = false;

    @Column(length = 20)
    private String auth;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;

    @Column(name = "income")
    private Double income;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "region")
    private String region;

    @Column(name = "district")
    private String district;

    @Column(name = "local")
    private String local;

    @Column(name = "marital_status")
    private Integer maritalStatus;

    @Column(name = "has_children")
    private Integer hasChildren;

    @Column(name = "housing_type")
    private String housingType;

    @Column(name = "is_login_locked", nullable = true)
    private Boolean isLoginLocked = false;  // false = 잠금 해제, true = 잠금됨

    @Column(name = "login_lock_time")
    private Long loginLockTime;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "expiration", nullable = true)
    private Timestamp expiration;
}
