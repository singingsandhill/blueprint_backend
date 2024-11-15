package com.chapter1.blueprint.member.repository;

import com.chapter1.blueprint.member.domain.FinanceRecommend;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyAlarmRepository extends JpaRepository<PolicyAlarm, Long> {

    PolicyAlarm findByUidAndPolicyIdx(Long uid, Long policyIdx);

    List<PolicyAlarm> findByNotificationEnabled(Boolean notificationEnabled);

    List<PolicyAlarm> findByUid(Long uid);
}

