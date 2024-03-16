package com.push.messenger.api.beans.batch.request;

import lombok.Data;

import java.util.List;

@Data
public class BatchRequest {
    private String campaignId;
    private List<Integer> customerId;
}
