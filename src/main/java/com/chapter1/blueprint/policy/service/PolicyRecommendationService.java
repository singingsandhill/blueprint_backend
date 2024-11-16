package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.member.service.MemberService;
import com.chapter1.blueprint.policy.domain.PolicyDetailFilter;
import com.chapter1.blueprint.policy.repository.PolicyDetailFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyRecommendationService {

    private final PolicyDetailFilterRepository policyDetailFilterRepository;
    private final MemberService memberService;

    public List<PolicyDetailFilter> getRecommendedPolicies(Long uid) {
        Member member = memberService.getMemberByUid(uid);

        Integer age = memberService.calculateAge(member.getBirthYear());

        return policyDetailFilterRepository.findRecommendedPoliciesByUid(member.getRegion(), age, member.getOccupation());
    }
}
