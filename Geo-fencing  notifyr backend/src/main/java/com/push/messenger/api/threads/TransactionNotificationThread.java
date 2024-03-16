package com.push.messenger.api.threads;

import static java.util.Objects.requireNonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.push.messenger.api.beans.batch.campaign.Campaign;
import com.push.messenger.api.beans.batch.customer.Customer;
import com.push.messenger.api.beans.batch.response.NotificationStatus;
import com.push.messenger.api.beans.batch.response.Status;
import com.push.messenger.api.beans.batch.response.StatusHandler;
import com.push.messenger.api.entity.executionMIS;
import com.push.messenger.api.service.MessagingService;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class TransactionNotificationThread implements Runnable{
	
	private final String title;
	private final String message;
	private final String imageUrl;
	private final List<Customer> customerList;
	private final List<executionMIS> misList;
	private final JdbcTemplate jdbcTemplate;
	private final Environment environment;
	private final MessagingService messagingService;
	private final String exid;
	private final String batchId;
	
    private JavaMailSender emailSender;

	@Override
	public void run() {
		try {
			execute();
		} catch (Exception e) {

			log.warn("exception details ", e);
		}
		
	}
	
	private void execute() {
		
		log.info("staring processing transaction notification id having title [ "
				+ title + " ] having batch Id [ " + exid + " ]");
		
		List<StatusHandler> responseList = new ArrayList<>();
		
		List<Customer> listOutput = customerList.stream()
				.filter(e -> misList.stream().anyMatch(customer -> e.getCustomerId().equals(customer.getCustomerId())))
				.collect(Collectors.toList());
		
		log.info("CustomerIds From Mis " + listOutput.size());
		
		if (!CollectionUtils.isEmpty(listOutput)) {
			listOutput.forEach(customer -> {

				if (customer != null && StringUtils.hasText(customer.getOperationSystem())
						&& StringUtils.hasText(customer.getDeviceId())) {
					log.info("operating system obtained is "
							+ customer.getOperationSystem() + " having device id [ " + customer.getDeviceId() + " ]");
					log.info("Transaction Data = " + message);
					
					Campaign campaign = new Campaign();
					
					
					if(imageUrl == null) {
						campaign.setBody(message);
						campaign.setTitle(title);
						campaign.setType("SILENT");
					}else {
						campaign.setBody(message);
						campaign.setTitle(title);
						campaign.setImage_url(imageUrl);
						campaign.setType("RICH");
					}
					
					NotificationStatus status = getNotificationStatus(
							customer.getOperationSystem().toLowerCase().trim(), campaign, customer);
					log.info("status obtained for [ " + customer.getCustomerId() + " ] status [ " + status
							+ " ] having name [ " + exid + " ]");
	

					responseList.add(StatusHandler.builder().responseId(requireNonNull(status).getStatus().getCode())
							.customerId(customer.getCustomerId()).status(requireNonNull(status.getStatus()).getDesc())
							.exeId(requireNonNull(exid)).build());
				}
			}			
			);			
			 
			log.info("Value In responseList [ " + responseList.size() + " ] at time [ " + LocalDateTime.now() + " ]");

			if (!CollectionUtils.isEmpty(responseList)) {
				int[] totalRecordsInserted = jdbcTemplate.batchUpdate(
						requireNonNull(environment.getProperty("query.update.mis")),
						new BatchPreparedStatementSetter() {
							@Override
							public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
								StatusHandler statusHandler = responseList.get(i);
								ps.setString(1, statusHandler.getResponseId());
								ps.setString(2, statusHandler.getStatus());
								ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
								ps.setString(4, statusHandler.getExeId());
								ps.setString(5, statusHandler.getCustomerId());

							}
							@Override
							public int getBatchSize() {
								return responseList.size();
							}
							
						});
				log.info("batch with Id " + exid + " is successfully inserted [ " + totalRecordsInserted.length + " ] at time [ " + LocalDateTime.now() + " ]");
			}
		}
		String updateQuery = "update transaction_execution_history set completed_at = ? where exe_id = ?";
        jdbcTemplate.update(updateQuery, LocalDateTime.now(), exid);
		
	}
	
	private NotificationStatus getNotificationStatus(String operatingSystem, final Campaign campaign,
			final Customer customer) {
		
		String body = campaign.getBody();
	    if (body.contains("&name&")) {
	        body = body.replace("&name&", customer.getFullName());
	    }

		String response = null;
		
		//Email sender
		
		String emailId = customer.getEmailId();
		
		if(!emailId.equals(null) && !emailId.isEmpty()) {			
				
	try {
		String htmlBody;
		
		if(("SILENT").equals(campaign.getType())) {
			htmlBody = "<html><body>" +
				    "<h2>"+campaign.getTitle()+"</h2>" +
					"<p>"+campaign.getBody()+"</p>"+
				    "</body></html>";
		}else {
			htmlBody = "<html><body>" +
				    "<h2>"+campaign.getTitle()+"</h2>" +
					"<p>"+campaign.getBody()+"</p>"+
					"<img src='cid:imageId'>" +
				    "</body></html>";
		}
		
		
		MimeMessage mimeMessage = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		helper.setTo(emailId);
		helper.setSubject(campaign.getTitle());
		helper.setText(htmlBody, true); // Set the content as HTML

		if(("RICH").equals(campaign.getType())) {
			ClassPathResource image = new ClassPathResource(campaign.getImage_url());
			helper.addInline("imageId", image);
		}
			
		emailSender.send(mimeMessage);

	} catch (MailException e) {
		// TODO Auto-generated catch block
		
	} catch (MessagingException e) {
		// TODO Auto-generated catch block
		
	}
			

		}
		
		
		switch (operatingSystem) {
		case "android":

			if (("SILENT").equals(campaign.getType())) {
				response = messagingService.sendAndroidMessage(
						Message.builder().setNotification(Notification.builder()
								.setBody(body)
								.setTitle(campaign.getTitle())
								.build())
								.setToken(customer.getDeviceId()).build(), campaign, customer);
			} 
			else {				
				Map<String,String> data = new HashMap<>();
				data.put("title", campaign.getTitle());
				data.put("body", body);
				data.put("url", campaign.getImage_url());				

				response = messagingService.sendAndroidMessage(Message.builder()
						
						.setNotification(Notification.builder().setBody(body)
								.setTitle(campaign.getTitle())
								.setImage(campaign.getImage_url())
								.build()).putAllData(data)
						.setToken(customer.getDeviceId()).build(), campaign, customer);
			}
			return (buildNotificationStatus(response));
		case "iphone":

			return buildNotificationStatus(
					messagingService.sendIosMessage(customer.getDeviceId(), campaign.getBody(), campaign.getTitle()));
			
		case "webapp" :
			
			if(("SILENT").equals(campaign.getType())) {
				response = messagingService.sendWebAppMessages(
						Message.builder().setNotification(Notification.builder()
								.setBody(body)
								.setTitle(campaign.getTitle())
								.build())
								.setToken(customer.getDeviceId()).build());
			}else {
				
				response = messagingService.sendWebAppMessages(
						Message.builder().setNotification(Notification.builder()
								.setBody(body)
								.setTitle(campaign.getTitle())
								.setImage(campaign.getImage_url())
								.build())
								.setToken(customer.getDeviceId()).build());
			}
			
			String res = response.replace("messages/", "messages/0:");
			
			log.info(res);
			
			return (buildNotificationStatus(res));
			
			
			
		default:
			return NotificationStatus.builder().status(Status.builder().code("FAILED").desc("INVALID OS").build())
					.build();
		}
	}
	
	private NotificationStatus buildNotificationStatus(String response) {
		NotificationStatus status;
		if (response.split(":")[1].equalsIgnoreCase("failed")) {
			status = NotificationStatus.builder()
					.status(Status.builder().code("000000").desc("failed".toUpperCase()).build()).build();
		} else {
			status = NotificationStatus.builder()
					.status(Status.builder().code(response.split(":")[1]).desc("Success".toUpperCase()).build())
					.build();
		}
		return status;
	}

}
