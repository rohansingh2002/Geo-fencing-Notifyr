package com.push.messenger.api.service;

import com.push.messenger.api.caches.CampaignCache;
import com.push.messenger.api.caches.MisCache;
import com.push.messenger.api.caches.UserCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final UserCache userCache;
    
    private final MisCache misCache;

    private final CampaignCache campaignCache;

    public void refreshUserCache() {
        userCache.load();
    }

    public void refreshCampaignCache() {
        campaignCache.load();
    }
    
    public void refreshMisCache() {
    	misCache.load();
    }
}
