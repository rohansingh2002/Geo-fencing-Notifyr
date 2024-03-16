package com.push.messenger.api.controller;

import com.push.messenger.api.beans.batch.request.BatchRequest;
import com.push.messenger.api.beans.batch.response.BatchResponse;
import com.push.messenger.api.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("batch")
@RequiredArgsConstructor
public class BatchController {

    private final PushService pushService;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<BatchResponse> createCampaign(final @RequestBody BatchRequest batchRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pushService.createCampaign(batchRequest));
    }
}
