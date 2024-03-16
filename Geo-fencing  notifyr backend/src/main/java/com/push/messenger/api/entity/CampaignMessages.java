package com.push.messenger.api.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.push.messenger.api.beans.batch.campaign.Campaign;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Campaign_Messages")
@Getter
@Setter
@ToString
@Builder
public class CampaignMessages {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long camMessgId;
	private String header;
	private String languageCode;
	private String body;
	private long campaignId;

}
