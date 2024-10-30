package com.chapter1.blueprint.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter @Setter
public class FinanceRecommend {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;

    @Column(name = "uid")
    private Long uid;

    @Column(name = "recommend_item")
    private String recommendItem;

    @Column(name = "recommend_reason")
    private String recommendReason;

    @Column(name = "recommend_date")
    private Date recommendDate;
}
