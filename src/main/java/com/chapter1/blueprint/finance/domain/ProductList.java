package com.chapter1.blueprint.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProductList {
    @Id
    @GeneratedValue
    @Column(name = "idx")
    private Long idx;
}
