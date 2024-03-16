package com.push.messenger.api.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.push.messenger.api.beans.batch.customer.Customer;
import com.push.messenger.api.caches.CampaignCache;
import com.push.messenger.api.caches.MisCache;
import com.push.messenger.api.caches.TransactionMisCache;
import com.push.messenger.api.caches.UserCache;
import com.push.messenger.api.entity.TransactionExeHistory;
import com.push.messenger.api.entity.executionMIS;
import com.push.messenger.api.repository.PushNotificationRepository;
import com.push.messenger.api.repository.TransactionExeHistoryRepository;
import com.push.messenger.api.service.MessagingService;
import com.push.messenger.api.threads.TransactionNotificationThread;
import com.push.messenger.api.threadPool.TransactionThreadPool;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@EnableScheduling
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionPushMessengerScheduler {
	
    private final PushNotificationRepository pushNotificationRepository;
    private final TransactionExeHistoryRepository transactionExeHistoryRepository;
    private final TransactionThreadPool transactionThreadPool;
    private final JdbcTemplate jdbcTemplate;
    private final UserCache userCache;
    private final TransactionMisCache misCache;
    private final Environment environment;
    private final MessagingService messagingService;
    private final CampaignCache campaignCache;
    
    String exid = "";
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Scheduled(fixedDelay = 1000)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void transactionPushMessageScheduler() {
    	
    	log.info("Transaction Scheduler");
    	
    	final List<TransactionExeHistory> pushNotificationListPreFilter = transactionExeHistoryRepository.findByExecutionStatus(0, PageRequest.of(0, 998, Sort.by("createdAt")));
    	
    	final List<TransactionExeHistory> pushNotificationList = new ArrayList<>();
    	
    	for(TransactionExeHistory exeHis : pushNotificationListPreFilter) {
    		
    		
    		if(exeHis.getScheduledAt() == null) {
    			pushNotificationList.add(exeHis);
    		}else {
    			if (exeHis.getScheduledAt() != null && (exeHis.getScheduledAt().isBefore(LocalDateTime.now()) || exeHis.getScheduledAt().isEqual(LocalDateTime.now()))) {
    				log.info("The past date and time is before or equal to the current date and time.");
                    pushNotificationList.add(exeHis);
                }
    		}
    	}
    	
    	log.info("pushNotificationListttttttt :{}",pushNotificationList);
        if (!CollectionUtils.isEmpty(pushNotificationList)) {
        	log.info("list of customer ids found !!!!!");

        	long campaignCount = jdbcTemplate.queryForObject("SELECT count(*) from engage_campaign where campaign_status='ACTIVE'", Integer.class);
        	log.info("campaign count = "+campaignCount);
        	
        	long userCount = jdbcTemplate.queryForObject("SELECT count(*) from engage_subscription where is_notification_on ='YES'", Integer.class);
        	log.info("user count = "+userCount);
        	
        	long campaignCountCache = campaignCache.getSize();
        	log.info("campaign in cache = "+campaignCountCache);
        	
        	long userCountCache = userCache.getSize();

        	
        	misCache.getSize();
        	
        	if(campaignCount != campaignCountCache) {
        		log.info("refreshing campaign cache");
            	campaignCache.refreshCampaignCache();
        	}
        	
        	if (userCount != userCountCache) {
            	log.info("refreshing user cache");
                userCache.refreshUserCache();

        	}
        	log.info("data in Mis Cache : " +  misCache.getSize());
        	if (misCache.getSize() == 0) {
        		log.info("refreshing mis Cache");
        		misCache.refreshMisCache();       		
        	}       	
                 	
        	List<String> idList = pushNotificationList.parallelStream().map(TransactionExeHistory:: getExeId).collect(Collectors.toList());
 
//        	List<String> campId = pushNotificationList.parallelStream().map(ExecutionHistory:: getCampaignId).collect(Collectors.toList());
        	
//        	List<Long> campIdList = campId.stream()
//                    .map(Long::valueOf)
//                    .collect(Collectors.toList());
        	
            int rowsUpdated = transactionExeHistoryRepository.updateTransactionExeHistoryBasedOnExecutionStatusAndExeIdIn(1, idList);
            log.info("total no of notifications updated are [ " + rowsUpdated + " ] with id [ " + idList + " ]");

            var pairList = pushNotificationList.parallelStream().map(notification ->            
            Pair.builder().
            				ExeId(notification.getExeId()).
            				message(notification.getMessage()).
            				title(notification.getTitle()).
            				imageUrl(notification.getImageUrl()).
            				build()).
            				collect(Collectors.toList());
            
            
            if (!CollectionUtils.isEmpty(pairList)) {
                pairList.forEach(p -> {
                    List<Customer> customerList;
                    
                    List<executionMIS> misLst = new ArrayList<>();
                    
                    if(p.ExeId!= null) {
                    	log.info("total SIZE OF mIs Cache " + misCache.getSize());
                    	

                    	exid = p.ExeId;
                    	
                    
                    	misLst = Lists.newArrayList(misCache.getMisDetailsByExeId(p.ExeId));
                    	
                    	log.info("MISLISTtttt : {} ", misLst.size() , exid);
                    }
                    
                    if (p.customerId!=null) {
                        customerList = Lists.newArrayList(userCache.getUserDetailsByCustomerId(p.customerId.trim()));
                        log.info("checking customer list " +customerList.toString());
                    } else  {
                        customerList =  userCache.getUserDetailsByNotification("YES");                        
                    }

                    log.info("customer list obtained for processing [ " + customerList.size() + " ] ");
                    if (!CollectionUtils.isEmpty(misLst)) {
                    	
                    	
                    	long c = 0;
                        for (int i = 0; i < misLst.size(); i = i + 100) {
                            List<executionMIS> subList;
                            if ((i + 100) > misLst.size()) {
                                subList = misLst.subList(i, misLst.size());
                               
                            } else {
                                subList = misLst.subList(i , i + 100);
                            }
                            
                            try {
                            	transactionThreadPool.executeRunnableTask(new TransactionNotificationThread(p.title, p.message, p.imageUrl, customerList , subList, jdbcTemplate,environment, messagingService, exid, p.customerId, emailSender));
                            	log.info("title ->"+ p.title+" message ->"+ p.message);
                            	log.info("customer list-->"+ customerList);
                            	log.info("sublist-->"+ subList);
                            } catch (RejectedExecutionException e) {
                                log.warn("thread pool is full hence pool is rejecting the request having exeId [ " + p.ExeId + " ] hence updating the request to 0 will be picked once pool is free", e);
                            }
                            c++;
                        }
                        log.info("count of how much time loop executed "+ c);
                        
                        
                   
                       
                    } else {
                        log.warn("there is no customer data obtained for the batch id [ " + p.ExeId + " ]");
                    }
                });
            }
            misCache.destroy();         
            transactionExeHistoryRepository.updateTransactionExeHistoryBasedOnExecutionStatusAndExeIdInWithCurrentDate(2, idList, LocalDateTime.now());
//            notificationCampaignRepository.updateExecutedAtBasedOnCampaignId(LocalDateTime.now(),campIdList);
        }
    	
    	
    }
    
    @Builder
    public static class Pair{
    	String ExeId;
    	String idList;
    	String campaignType;
    	String campaignId;
    	String customerId;
    	String title;
    	String message;
    	String imageUrl;
    }

}
