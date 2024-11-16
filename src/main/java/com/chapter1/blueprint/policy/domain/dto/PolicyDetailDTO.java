package com.chapter1.blueprint.policy.domain.dto;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
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
    private String way;
    private Integer minAge;
    private Integer maxAge;
    private String job;

    public PolicyDetailDTO(Long idx, String subject, String condition, String content, String scale,
                           String enquiry, String document, String url, String way,
                           Integer minAge, Integer maxAge, String job) {
        this.idx = idx;
        this.subject = subject;
        this.condition = condition;
        this.content = content;
        this.scale = scale;
        this.enquiry = enquiry;
        this.document = document;
        this.url = url;
        this.way = way;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.job = job;
    }

}
