package com.push.messenger.api.service;

import com.google.common.collect.Lists;
import com.push.messenger.api.beans.batch.request.BatchRequest;
import com.push.messenger.api.beans.batch.response.BatchResponse;
import com.push.messenger.api.entity.ExecutionHistory;
//import com.push.messenger.api.entity.PushNotification;
import com.push.messenger.api.repository.PushNotificationRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PushService {

    private final PushNotificationRepository pushNotificationRepository;

    @SneakyThrows
    public BatchResponse createCampaign(final BatchRequest request) {
        if (CollectionUtils.isEmpty(request.getCustomerId())) {
            return CompletableFuture.completedFuture(this.createPushNotification(request)).
                    thenApply(pushNotificationRepository :: saveAndFlush).
                    thenApply(pushNotification ->  BatchResponse.builder().
                            id(Lists.newArrayList(pushNotification.getId())).build()).get();
        }

        return BatchResponse.builder().id(pushNotificationRepository.saveAll(createPushNotificationList(request)).
                stream().map(ExecutionHistory::getExeId).collect(Collectors.toList())).build();
    }


    private ExecutionHistory createPushNotification(final BatchRequest request) {
    	ExecutionHistory notification = new ExecutionHistory();
        notification.setCampaignId(request.getCampaignId());
//        notification.setId(UUID.randomUUID().toString());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setExecutionStatus(0);
        notification.setCampaignType("ALL");
//        notification.setCustomerId(null);
        return notification;
    }


    private List<ExecutionHistory> createPushNotificationList(final BatchRequest request) {
        List<ExecutionHistory> pushNotificationList = new ArrayList<>();
        request.getCustomerId().forEach(customerId -> {
        	ExecutionHistory notification = new ExecutionHistory();
            notification.setCampaignId(request.getCampaignId());
//            notification.setId(UUID.randomUUID().toString());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExecutionStatus(0);
//            notification.setCampaignType("INDIVIDUAL");
//            notification.setCustomerId(String.valueOf(customerId));
            pushNotificationList.add(notification);
        });

        return pushNotificationList;
    }


}
