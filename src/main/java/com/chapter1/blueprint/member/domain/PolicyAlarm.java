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
public class PolicyAlarm {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;

    @Column(name = "uid")
    private Long uid;

    @Column(name = "alarm_category")
    private String alarmCategory;

    @Column(name = "alarm_type")
    private String alarmType;

    @Column(name = "send_date")
    private Date sendDate;
}
