package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.member.service.MemberService;
import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.domain.dto.FilterDTO;
import com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO;
import com.chapter1.blueprint.policy.domain.dto.PolicyListDTO;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chapter1.blueprint.policy.repository.PolicyDetailRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyDetailService {
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyListRepository policyListRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    public List<PolicyListDTO> getPolicyList() {
        List<PolicyList> policyList = policyListRepository.findAll();

        return policyList.stream().map(policy -> {
            PolicyListDTO policyListDTO = new PolicyListDTO();
            policyListDTO.setIdx(policy.getIdx());
            policyListDTO.setCity(policy.getCity());
            policyListDTO.setDistrict(policy.getDistrict());
            policyListDTO.setType(policy.getType());
            policyListDTO.setName(policy.getName());
            policyListDTO.setOfferInst(policy.getOfferInst());
            policyListDTO.setManageInst(policy.getManageInst());
            policyListDTO.setStartDate(policy.getStartDate());
            policyListDTO.setEndDate(policy.getEndDate());
            policyListDTO.setApplyStartDate(policy.getApplyStartDate());
            policyListDTO.setApplyEndDate(policy.getApplyEndDate());
            return policyListDTO;
        }).collect(Collectors.toList());
    }

//    public PolicyDetailDTO getPolicyDetail(Long idx) {
//        return policyDetailRepository.findPolicyDetailByIdx(idx);
//    }

    public PolicyDetail getPolicyDetail(Long idx) {
        return policyDetailRepository.findById(idx).orElse(null);
    }

    public List<PolicyList> getPolicyListByFiltering(FilterDTO filterDTO) {
         return policyListRepository.findByCityDistrictTypeAgeJob(filterDTO.getCity(), filterDTO.getDistrict(), filterDTO.getType(), filterDTO.getAge(), filterDTO.getJob(), filterDTO.getName());
    }

    public List<PolicyList> recommendPolicy(Long uid) {
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Member not found with uid (recommendPolicy): " + uid));

        int age = memberService.calculateAge(member.getBirthYear());
        return policyListRepository.findByCityDistrictAgeJob(member.getRegion(), member.getDistrict(), age, member.getOccupation());
    }

    public List<PolicyList> getPeerPolicy(Long uid) {
        return policyListRepository.PeerPolicy(uid);
    }
}
