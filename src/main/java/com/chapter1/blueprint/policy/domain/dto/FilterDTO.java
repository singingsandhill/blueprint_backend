package com.chapter1.blueprint.policy.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterDTO {
    private String city;
    private String district;
    private String type;
    private Integer age;
    private String job;
    private String name;
}
