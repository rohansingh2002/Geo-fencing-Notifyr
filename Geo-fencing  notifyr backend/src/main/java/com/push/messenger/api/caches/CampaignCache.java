package com.push.messenger.api.caches;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.push.messenger.api.beans.batch.campaign.Campaign;
import com.push.messenger.api.entity.CampaignMessages;
import com.push.messenger.api.service.CacheService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.Column;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignCache {

    private final Cache<String, Campaign> campaignCache = CacheBuilder.newBuilder().concurrencyLevel(2).initialCapacity(100).build();
    private final JdbcTemplate jdbcTemplate;
    
   
//    private CacheService cacheService;
    
    
    @PostConstruct
    public void load() {
        log.info("loading the campaign cache starts at [ " + LocalDateTime.now() + " ]");

        List<Campaign> campaignList = jdbcTemplate.query("select campaign_id, body, body_hindi, body_marathi,title,notification_type, header, image_url, deep_link_url from engage_campaign where campaign_status='ACTIVE'", new ResultSetExtractor<List<Campaign>>() {
         

        	final List<Campaign> campaignList = new ArrayList<>();

            @Override
            public List<Campaign> extractData(final @NonNull ResultSet rs) throws DataAccessException, SQLException {
//            	log.info("RSS value ==> "+ rs);
                while (rs.next()) {
//                	 Blob cloumn = rs.getBlob("image_file");
//                	 byte[] val = cloumn.getBytes(cloumn.length(), 1);
                    campaignList.add(Campaign.builder().body(rs.getString("body")).id(rs.getString("campaign_id")).title(rs.getString("title")).type(rs.getString("notification_type")).header(rs.getString("header")).image_url(rs.getString("image_url")).body_hindi(rs.getString("body_hindi")).body_marathi(rs.getString("body_marathi")).deepLinkURL(rs.getString("deep_link_url")).build());
                }
//                log.info("campaignList :{}",campaignList);
                return campaignList;
            }
        });
        
        List<CampaignMessages> campaignMessagesList = jdbcTemplate.query("select cam_messg_id, body, campaign_id,header,language_code from campaign_messages", new ResultSetExtractor<List<CampaignMessages>>() {

        	final List<CampaignMessages> campaignMessagesList = new ArrayList<CampaignMessages>();

            @Override
            public List<CampaignMessages> extractData(final @NonNull ResultSet rs) throws DataAccessException, SQLException {
//            	log.info("RSS value ==> "+ rs);
                while (rs.next()) {
//                	 Blob cloumn = rs.getBlob("image_file");
//                	 byte[] val = cloumn.getBytes(cloumn.length(), 1);
                	campaignMessagesList.add(CampaignMessages.builder().campaignId(rs.getLong("cam_messg_id")).body(rs.getString("body")).campaignId(rs.getLong("campaign_id")).header(rs.getString("header")).languageCode(rs.getString("language_code")).build());
                }
//                log.info("campaignList :{}",campaignList);
                return campaignMessagesList;
            }
        });
        
        for(int i=0;i<campaignList.size();i++) {
        	List<CampaignMessages>list=new ArrayList<CampaignMessages>();
        	for(int j=0;j<campaignMessagesList.size();j++) {
        		if(Integer.parseInt(campaignList.get(i).getId())==campaignMessagesList.get(j).getCampaignId()) {
        			list.add(campaignMessagesList.get(j));
        		}
        	}
        	campaignList.get(i).setCampaignMessages(list);
        }
        

       if (!CollectionUtils.isEmpty(campaignList)) {
           campaignList.forEach(campaign -> campaignCache.put(campaign.getId(), campaign));
       }

        log.info("loading the campaign cache ends at [ " + LocalDateTime.now() + " ]");
    }
    
    public void refreshCampaignCache() {
       clear();
       load();
    }
    


    
    public Campaign getCampaign(final String campId) {
    	return campaignCache.getIfPresent(campId);
    	
    }
    
    public long getSize() {
    	return campaignCache.size();
    }

    @PreDestroy
    public void clear() {
//        campaignCache.cleanUp();
    	campaignCache.invalidateAll();
    }
}

