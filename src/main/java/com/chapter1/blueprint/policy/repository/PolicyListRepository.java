package com.chapter1.blueprint.policy.repository;

import com.chapter1.blueprint.policy.domain.dto.PolicyListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import com.chapter1.blueprint.policy.domain.PolicyList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyListRepository extends JpaRepository<PolicyList,Long> {

    @Query("SELECT p FROM PolicyList p " +
            "JOIN PolicyDetailFilter f ON p.idx = f.idx " +
            "WHERE (:city IS NULL OR p.city = :city) " +
            "AND (:district IS NULL OR p.district = :district) " +
            "AND (:type IS NULL OR p.type = :type) " +
            "AND (:age IS NULL OR (f.minAge <= :age AND f.maxAge >= :age)) " +
            "AND (:job IS NULL OR f.job = :job)" +
            "AND (:name IS NULL OR p.name LIKE %:name%)" +
            "ORDER BY p.applyEndDate DESC")
    List<PolicyList> findByCityDistrictTypeAgeJob(
            @Param("city") String city,
            @Param("district") String district,
            @Param("type") String type,
            @Param("age") Integer age,
            @Param("job") String job,
            @Param("name") String name);

    @Query("SELECT p FROM PolicyList p WHERE DATEDIFF(p.applyEndDate, CURRENT_DATE) = 3")
    List<PolicyListDTO> findPoliciesWithApproachingDeadline();

    @Query("SELECT p FROM PolicyList p " +
            "JOIN PolicyDetailFilter f ON p.idx = f.idx " +
            "WHERE (:city IS NULL OR p.city = :city) " +
            "AND (:district IS NULL OR p.district = :district OR p.district LIKE CONCAT('%', :district, '%')) " +
            "AND (:age IS NULL OR ((f.minAge <= :age AND f.maxAge >= :age) OR (f.minAge = 0 AND f.maxAge = 0))) " +
            "AND (:job IS NULL OR f.job = :job OR f.job = '전체') " +
            "AND (p.applyEndDate >= CURRENT_DATE)")
    List<PolicyList> findByCityDistrictAgeJob(
            @Param("city") String city,
            @Param("district") String district,
            @Param("age") Integer age,
            @Param("job") String job);

    @Query(value = """
    SELECT pl.* 
    FROM policy.policy_list pl
    JOIN (
        SELECT pa.policy_idx
        FROM member.policy_alarm pa
        JOIN member.member_info mi
            ON pa.uid = mi.uid
        WHERE mi.uid != :uid
          AND mi.birth_year BETWEEN (
              SELECT birth_year 
              FROM member.member_info 
              WHERE uid = :uid
          ) - 5
          AND (
              SELECT birth_year 
              FROM member.member_info 
              WHERE uid = :uid
          ) + 5
          AND mi.region = (
              SELECT region 
              FROM member.member_info 
              WHERE uid = :uid
          )
        GROUP BY pa.policy_idx
        ORDER BY COUNT(pa.policy_idx) DESC
        LIMIT 3
    ) top_policies
    ON pl.idx = top_policies.policy_idx
    """, nativeQuery = true)
    List<PolicyList> PeerPolicy(@Param("uid") Long uid);

}
