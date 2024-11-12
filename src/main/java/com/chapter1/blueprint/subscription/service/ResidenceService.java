package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.subscription.domain.dto.ResidenceDTO;
import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ResidenceService {
    private final RealEstatePriceRepository realEstatePriceRepository;

    public List<String> getCityList() {
        return realEstatePriceRepository.getCityList();
    }

    public List<String> getDistrict(ResidenceDTO residenceDTO) {
        return realEstatePriceRepository.getDistrict(residenceDTO.getCity());
    }

    public List<String> getLocal(ResidenceDTO residenceDTO) {
        return realEstatePriceRepository.getLocal(residenceDTO.getCity(), residenceDTO.getDistrict());
    }
}
