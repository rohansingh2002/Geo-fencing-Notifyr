package com.push.messenger.api.beans.batch.response;

import com.google.cloud.Timestamp;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class StatusHandler {
	private Long id;
    private String responseId;
    private String customerId;
    private String status;
    private String campaignId;
    private String exeId;
    
}
