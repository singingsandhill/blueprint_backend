package com.chapter1.blueprint.member.repository;

import com.chapter1.blueprint.member.domain.FinanceRecommend;
import com.chapter1.blueprint.member.domain.PolicyAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyAlarmRepository extends JpaRepository<PolicyAlarm, Long> {
}
