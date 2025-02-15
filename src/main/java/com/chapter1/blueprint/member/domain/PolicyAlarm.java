package com.chapter1.blueprint.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "policy_alarm", catalog = "member")
public class PolicyAlarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "policy_idx")
    private Long policyIdx;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = false;

    @Column(name = "apply_end_date")
    private Date applyEndDate;

    @Column(name = "is_read")
    private Boolean isRead = false;
}
