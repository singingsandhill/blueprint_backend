package com.chapter1.blueprint.policy.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "policy",catalog = "policy_category")
public class PolicyCategory {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;

    @Column(name="type")
    private String type;
}
