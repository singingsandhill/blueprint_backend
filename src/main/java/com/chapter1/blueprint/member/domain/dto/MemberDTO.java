package com.chapter1.blueprint.member.domain.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long uid;
    private String memberId;
    private String password;
    private String memberName;
    private String email;
    private Integer birthYear;
    private String birth;
    private String gender;
    private String profile;
    private String auth;
    private Integer isLoginLocked;
    private Long loginLockTime;
    private Boolean agreementService;
    private Boolean agreementInfo;

}
