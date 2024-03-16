package com.push.messenger.api.controller;

import com.push.messenger.api.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refresh/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @GetMapping("/user")
    public ResponseEntity<String> refreshUserCache() {
        cacheService.refreshUserCache();
        return ResponseEntity.ok("user cache is refreshed successfully");
    }

    @GetMapping("/campaign")
    public ResponseEntity<String> refreshCampaignCache() {
        cacheService.refreshCampaignCache();
        return ResponseEntity.ok("campaign cache is refreshed successfully");
    }
}
