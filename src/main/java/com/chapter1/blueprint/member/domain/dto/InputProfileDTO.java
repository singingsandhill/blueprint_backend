package com.chapter1.blueprint.member.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputProfileDTO {
    private Double income;
    private String occupation;
    private String residence;
    private Integer maritalStatus;
    private Integer hasChildren;
    private String housingType;
}
