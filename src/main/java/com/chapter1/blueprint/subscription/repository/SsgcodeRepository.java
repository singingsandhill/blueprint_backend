package com.chapter1.blueprint.subscription.repository;

import com.chapter1.blueprint.subscription.domain.Ssgcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SsgcodeRepository extends JpaRepository<Ssgcode, Integer> {

    //@Query("SELECT s FROM Ssgcode s")
    @Query("SELECT s FROM Ssgcode s GROUP BY s.ssgCd5")
    List<Ssgcode> findAllSsgcodes();

    @Query("SELECT s FROM Ssgcode s WHERE s.ssgCd5 = :number")
    Ssgcode findBySsgCd5(@Param("number") String number);

}
