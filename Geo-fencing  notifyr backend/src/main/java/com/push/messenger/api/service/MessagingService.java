package com.push.messenger.api.service;

import static com.push.messenger.api.constants.QueryConstants.ACCOUNT_SID;
import static com.push.messenger.api.constants.QueryConstants.AUTH_TOKEN;
import static com.push.messenger.api.constants.QueryConstants.FROM_NUMBER;
import static com.push.messenger.api.constants.QueryConstants.STR_QUERY_APN_KEY_ALL;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.push.messenger.api.beans.batch.campaign.Campaign;
import com.push.messenger.api.beans.batch.customer.Customer;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingService {

    private final FirebaseMessaging firebaseMessaging;
    private final ApnsClient apnsClient;
    private final JdbcTemplate jdbcTemplate;

    public ApiFuture<BatchResponse> sendAndroidMessage(Message message) {
        return firebaseMessaging.sendAllAsync((List<Message>) message);
    }

    public String sendAndroidMessage(final Message message, Campaign campaign, Customer customer) {
        try {
            return firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {

        	// Use Twilio SMS gateway to send the notification
        	try {
        		String body = campaign.getBody();
        	    if (body.contains("&name&")) {
        	        body = body.replace("&name&", customer.getFullName());
        	    }
        		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        		com.twilio.rest.api.v2010.account.Message sms = com.twilio.rest.api.v2010.account.Message.creator(
                        new PhoneNumber("+91"+customer.getMobileNumber()),
                        new PhoneNumber(FROM_NUMBER),
                        campaign.getTitle() + ": " + body)
                        .create();
        		 log.info("Notification sent through Twilio SMS gateway: " + sms.getSid());
                 return "0101:SMS_SENT";
        	}catch (Exception ex) {
        		log.error("Failed to send notification through Twilio SMS gateway: " + ex.getMessage(), ex);
                return "0101:FAILED";
			}
            
        } catch (Exception e) {
            log.error("Generic exception arises while sending message to android " + message, e);
            return "0101:FAILED";
        } 
    }
    
    
    public String sendWebAppMessages(Message messages) {
    	try {
			return firebaseMessaging.send(messages);
		} catch (FirebaseException e) {
			 log.error("Generic exception arises while sending message to android " +e.getMessage());
	            return "0101:FAILED";
		}
    }
    

    public String sendIosMessage(String deviceToken, String alertBody, String title) {
        try {
        	List<Tuple> tuple = createTuples(STR_QUERY_APN_KEY_ALL);
        	log.info("tuple :{}",tuple);
        	
            final ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
            payloadBuilder.setAlertBody(alertBody);
            payloadBuilder.setAlertTitle(title);
            final String payload = payloadBuilder.build();
            final String token = TokenUtil.sanitizeTokenString(deviceToken);

            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    apnsClient.sendNotification(new SimpleApnsPushNotification(token,tuple.get(0).getValue0(),payload)).get();

            if (pushNotificationResponse.isAccepted()) {
                return pushNotificationResponse.getApnsId() + ":SUCCESS";
            } else {
                return pushNotificationResponse.getRejectionReason().get() + ":FAILED";
            }
        } catch (Exception e) {
            log.error("Generic exception arises while sending message to ios" + alertBody, e);
            return "0101:FAILED";
        }
    }
    
    private List<Tuple> createTuples(String query) {
        return  jdbcTemplate.query(query, (rs, rowNum) ->
                Tuple.builder().fileData(rs.getBytes(1)).password(rs.getString(2)).value0(rs.getString(3)).build());
    }
}

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Tuple {
   private byte[] fileData;
   private String password;
   private String value0;
}
