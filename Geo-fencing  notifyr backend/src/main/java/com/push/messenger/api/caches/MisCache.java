package com.push.messenger.api.caches;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.push.messenger.api.entity.ExecutionHistory;
import com.push.messenger.api.entity.TransactionExeHistory;
import com.push.messenger.api.entity.executionMIS;
import com.push.messenger.api.repository.ExecutionMisRepository;
import com.push.messenger.api.repository.PushNotificationRepository;
import com.push.messenger.api.repository.TransactionExeHistoryRepository;
import com.push.messenger.api.scheduler.TransactionPushMessengerScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MisCache {
	
	private final Cache<String, List<executionMIS>> misCache =   CacheBuilder.newBuilder().concurrencyLevel(2).initialCapacity(1000).build();
	 
	 @Autowired
	 private PushNotificationRepository pushNotificationRepository;
	 
	 @Autowired
	 private TransactionExeHistoryRepository transactionExeHistoryRepository;
	 
	 private final ExecutionMisRepository executionMisrepository;
	 
	 
	 public List<executionMIS> getMisDetailsByExeId(String ExeId) {
	        return misCache.getIfPresent(ExeId);
	    }
	   
	 public long getSize() {
	    	return misCache.size();
	    }

	@PostConstruct
	public void load() {
		log.info("loading the mis cache starts at [ " + LocalDateTime.now() + " ]");
		final List<ExecutionHistory> pushNotificationList = pushNotificationRepository.findByExecutionStatus(0, PageRequest.of(0, 998, Sort.by("createdAt")));
//		 List<List<executionMIS>> misList = new ArrayList<>();
			pushNotificationList.forEach(history ->{
				List<executionMIS> mis = new ArrayList<>();
				mis = executionMisrepository.findByExeId(history.getExeId());
				misCache.put(history.getExeId(), mis);
			        	});
			
			log.info("MisCacheeeeeee "+ misCache.size());
			
//			log.info("loading the mis cache ends at [ " + new Date(System.currentTimeMillis()) + " ]");
	}
	
	 public void refreshMisCache() {
	       destroy();
	       load();
	       
	       
	    }
	 
	 @PreDestroy
	    public void destroy() {
		 misCache.invalidateAll();
	 }
}
