package com.push.messenger.api.beans.batch.mis;


import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class PushMIS {
//    private String id;
//    private String batchId;
//    private String campaignType;
    private String deliveredStatus;
    private String responseId;
//    private String customerId;
}

