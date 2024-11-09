package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.dto.FilterDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import com.chapter1.blueprint.policy.domain.PolicyList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyListRepository extends JpaRepository<PolicyList,Long> {

    @Query("SELECT p FROM PolicyList p " +
            "WHERE (:district IS NULL OR p.district = :district) " +
            "AND (:type IS NULL OR p.type = :type)")
    List<PolicyList> findByDistrictAndType(@Param("district") String district, @Param("type")String type);

}
