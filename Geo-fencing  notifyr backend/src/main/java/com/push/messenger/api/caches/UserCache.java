package com.push.messenger.api.caches;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.push.messenger.api.beans.batch.customer.Customer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCache {

    private final Cache<String, List<Customer>> userCache = CacheBuilder.newBuilder().concurrencyLevel(2).initialCapacity(1000).build();
    private final Cache<String, Customer> userCacheByCustomer = CacheBuilder.newBuilder().concurrencyLevel(2).initialCapacity(1000).build();
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public List<Customer> getUserDetailsByNotification(final String notification) {
        return userCache.getIfPresent(notification);
    }

    public Customer getUserDetailsByCustomerId(final String customerId) {
        return userCacheByCustomer.getIfPresent(customerId);
    }
    public long getSize() {
    	
    	return userCacheByCustomer.size();
    	
    }
    


    @PostConstruct
    public void load() {
        log.info("loading the user cache starts at [ " + LocalDateTime.now() + " ]");
        List<Customer> customerList =  jdbcTemplate.query(Objects.requireNonNull(environment.getProperty("query.select.messenger")), new ResultSetExtractor<List<Customer>>() {
            final List<Customer> customerList = new ArrayList<>();
            @Override
            public List<Customer> extractData(final @NonNull ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    customerList.add(Customer.builder().
                                id(rs.getString("id")).
                                deviceId(rs.getString("device_id")).
                                deviceName(rs.getString("device_name")).
                                emailId(rs.getString("email_id")).
                                mobileNumber(rs.getString("mobile_no")).
                                customerId(rs.getString("customer_id")).
                                is_notification_on(rs.getString("is_notification_on")).
                                operationSystem(rs.getString("os_name")).
                                fullName(rs.getString("full_name")).
                                var1(rs.getString("var1")).
                                var2(rs.getString("var2")).
                                var3(rs.getString("var3")).
                                language(rs.getString("language")).
                            build());
                }
                return customerList;
            }
        });
       
        if (!CollectionUtils.isEmpty(customerList)) {
        	log.info("inside if condition == "+customerList.size() );
            customerList.forEach(customer -> {
                List<Customer> list = userCache.getIfPresent(customer.getIs_notification_on());
//                log.info("inside ");
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(customer);
                userCache.put(customer.getIs_notification_on(), list);
//                log.info("length of list created = "+list.size());
            });
            customerList.forEach(customer -> userCacheByCustomer.put(customer.getCustomerId(), customer));
           
        }
     

        log.info("loading the user cache ends at [ " + new Date(System.currentTimeMillis()) + " ]");

    }
    
    public void refreshUserCache() {
       destroy();
       load();
    }
    

    @PreDestroy
    public void destroy() {
    	
        userCache.invalidateAll();
        userCacheByCustomer.invalidateAll();
     
    }
}
