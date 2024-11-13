package com.chapter1.blueprint.policy.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PolicyListDTO {
    private Long idx;
    private String city;
    private String district;
    private String type;
    private String name;
    private String offerInst;
    private String manageInst;
    private Date startDate;
    private Date endDate;
    private Date applyStartDate;
    private Date applyEndDate;
}
