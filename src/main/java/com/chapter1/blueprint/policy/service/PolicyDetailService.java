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

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyDetailService {
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyListRepository policyListRepository;

    public PolicyListDTO getPolicyList(Long idx) {
        PolicyList policyList = policyListRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("해당 idx의 PolicyList를 찾을 수 없습니다."));

        PolicyListDTO policyListDTO = new PolicyListDTO();
        policyListDTO.setIdx(policyList.getIdx());
        policyListDTO.setCity(policyList.getCity());
        policyListDTO.setDistrict(policyList.getDistrict());
        policyListDTO.setType(policyList.getType());
        policyListDTO.setName(policyList.getName());
        policyListDTO.setOfferInst(policyList.getOfferInst());
        policyListDTO.setManageInst(policyList.getManageInst());
        policyListDTO.setStartDate(policyList.getStartDate());
        policyListDTO.setEndDate(policyList.getEndDate());
        policyListDTO.setApplyStartDate(policyList.getApplyStartDate());
        policyListDTO.setApplyEndDate(policyList.getApplyEndDate());

        return policyListDTO;
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

        return policyDetailDTO;
    }
}
