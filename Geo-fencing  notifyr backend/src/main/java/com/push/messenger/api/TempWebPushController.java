package com.push.messenger.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.push.messenger.api.service.MessagingService;

@RestController()
@RequestMapping("/engage")
public class TempWebPushController {
	
	@Autowired
	MessagingService messagingService;
	
	@PostMapping("/web/silent")
	public String sendWebNotification(@RequestParam String token) {
		
		System.out.println("token ->" +token);
		
    	Message messages = Message.builder().setNotification(Notification.builder()
				.setBody("This is a test body for web push notification")
				.setTitle("Web push notification")
				.build())
				.setToken(token).build();
		
		return messagingService.sendWebAppMessages(messages);
		
	}
	
	@PostMapping("/web/rich")
	public String sendWebNotificationRich(@RequestParam String token) {
		
		System.out.println("token ->" +token);
		
    	Message messages = Message.builder().setNotification(Notification.builder()
				.setBody("This is a test body for web push notification")
				.setTitle("Web push notification")
				.setImage("https://media.istockphoto.com/id/1322277517/photo/wild-grass-in-the-mountains-at-sunset.jpg?s=612x612&w=0&k=20&c=6mItwwFFGqKNKEAzv0mv6TaxhLN3zSE43bWmFN--J5w=")
				.build())
				.setToken(token).build();
		
		return messagingService.sendWebAppMessages(messages);
		
	}

}
