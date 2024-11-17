package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.SubscriptionList;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionListRepository extends JpaRepository<SubscriptionList, Long> {

    List<SubscriptionList> findByRegionAndCityAndDistrictContaining(String region, String city, String district);

}
