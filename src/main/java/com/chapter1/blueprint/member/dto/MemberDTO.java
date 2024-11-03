package com.chapter1.blueprint.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long uid;
    private String id;
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
}
