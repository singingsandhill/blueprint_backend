package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO;
import com.chapter1.blueprint.policy.domain.dto.PolicyListDTO;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chapter1.blueprint.policy.repository.PolicyDetailRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyDetailService {
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyListRepository policyListRepository;

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

    public PolicyDetailDTO getPolicyDetail(Long idx) {
        PolicyDetail policyDetail = policyDetailRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("해당 idx의 PolicyDetail을 찾을 수 없습니다."));

        PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
        policyDetailDTO.setIdx(policyDetail.getIdx());
        policyDetailDTO.setSubject(policyDetail.getSubject());
        policyDetailDTO.setCondition(policyDetail.getCondition());
        policyDetailDTO.setContent(policyDetail.getContent());
        policyDetailDTO.setScale(policyDetail.getScale());
        policyDetailDTO.setEnquiry(policyDetail.getEnquiry());
        policyDetailDTO.setDocument(policyDetail.getDocument());
        policyDetailDTO.setUrl(policyDetail.getUrl());
        policyDetailDTO.setWay(policyDetail.getWay());

        return policyDetailDTO;
    }
}
