package com.chapter1.blueprint.member.repository;

import com.chapter1.blueprint.member.domain.FinanceRecommend;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Repository
public interface PolicyAlarmRepository extends JpaRepository<PolicyAlarm, Long> {

    PolicyAlarm findByUidAndPolicyIdx(Long uid, Long policyIdx);

    List<PolicyAlarm> findByNotificationEnabled(Boolean notificationEnabled);

    List<PolicyAlarm> findByUid(Long uid);

    List<PolicyAlarm> findByUidAndAlarmType(Long uid, String alarmType);

    List<PolicyAlarm> findAllByNotificationEnabled(Boolean notificationEnabled);

    @Query("SELECT p FROM PolicyAlarm p WHERE p.notificationEnabled = true AND p.applyEndDate <= :applyEndDate")
    List<PolicyAlarm> findEnabledNotificationsBeforeDeadline(@Param("applyEndDate") Date applyEndDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM PolicyAlarm pa WHERE pa.policyIdx = :policyIdx")
    void deleteByPolicyIdx(@Param("policyIdx") Long policyIdx);
}

