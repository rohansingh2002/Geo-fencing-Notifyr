package com.push.messenger.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.push.messenger.api.entity.NotificationsCompaign;

@Repository
@Transactional
public interface NotificationCampaignRepository extends JpaRepository<NotificationsCompaign, Long> {

	@Modifying
	@Query("update NotificationsCompaign p set p.executedAt = :executed_at where p.campaignId in (:campaign_id)")
	void updateExecutedAtBasedOnCampaignId(@Param("executed_at") LocalDateTime localDate, @Param("campaign_id") List<Long> campaignId);
}
