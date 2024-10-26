package com.chapter1.blueprint.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    // JPA 특성상 만들어 놓은 생성자
    protected Address() {}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
