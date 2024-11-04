package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.dto.PolicyDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chapter1.blueprint.policy.repository.PolicyDetailRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyDetailService {
    private final PolicyDetailRepository policyDetailRepository;

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
