package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.RealEstatePrice;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealEstatePriceRepository extends JpaRepository<RealEstatePrice, Long> {
}
