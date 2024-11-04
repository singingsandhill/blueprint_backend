package com.chapter1.blueprint.policy.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyDetailDTO {
    private Long idx;
    private String subject;
    private String condition;
    private String content;
    private String scale;
    private String enquiry;
    private String document;
    private String url;
}
