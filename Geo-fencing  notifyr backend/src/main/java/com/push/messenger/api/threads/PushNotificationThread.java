package com.push.messenger.api.threads;

import static java.util.Objects.requireNonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.push.messenger.api.beans.batch.campaign.Campaign;
import com.push.messenger.api.beans.batch.customer.CustomObject;
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
public class PushNotificationThread implements Runnable {

	private final String type;
	private final String campaignId;
	private final List<Customer> customerList;
	private final List<executionMIS> misList;

	private final JdbcTemplate jdbcTemplate;
	private final Environment environment;
	private final MessagingService messagingService;
	private final Campaign campaign;
	private final String exid;
	private final String batchId;

	private JavaMailSender emailSender;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void run() {
		try {
			execute();
		} catch (Exception e) {

			log.warn("exception details ", e);
		}
	}

	private void execute() {

		log.info("staring processing push notification id which is of type [ " + type + " ] having campaign Id [ "
				+ campaignId + " ] having batch Id [ " + exid + " ]");
		List<StatusHandler> responseList = new ArrayList<>();

		List<Customer> listOutput = customerList.stream()
				.filter(e -> misList.stream().anyMatch(customer -> e.getCustomerId().equals(customer.getCustomerId())))
				.collect(Collectors.toList());

		List<CustomObject> list = new ArrayList<>();

		for (Customer l : listOutput) {
			for (executionMIS mis : misList) {
				if (l.getCustomerId().equals(mis.getCustomerId())) {
					list.add(new CustomObject(l, mis));
				}
			}
		}

		log.info("CustomerIds From Mis " + listOutput.size());

		if (!CollectionUtils.isEmpty(list)) {
			list.forEach(l -> {

				if (l != null && StringUtils.hasText(l.getCustomer().getOperationSystem())
						&& StringUtils.hasText(l.getCustomer().getDeviceId())) {
					log.info("operating system obtained for campaign id [ " + campaignId + " ] "
							+ l.getCustomer().getOperationSystem() + " having device id [ "
							+ l.getCustomer().getDeviceId() + " ]");
					log.info("Campaign Data = " + campaign.getBody());

					NotificationStatus status = getNotificationStatus(
							l.getCustomer().getOperationSystem().toLowerCase().trim(), campaign, l);
					log.info("status obtained for [ " + l.getCustomer().getCustomerId() + " ] status [ " + status
							+ " ] having name [ " + exid + " ]");

					// Fix Needed Here

					responseList.add(StatusHandler.builder().responseId(requireNonNull(status).getStatus().getCode())
							.customerId(l.getCustomer().getCustomerId())
							.status(requireNonNull(status.getStatus()).getDesc())
							.campaignId(requireNonNull(this.campaignId)).exeId(requireNonNull(exid)).build());
				}
			});

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
				log.info("batch with Id " + exid + " is successfully inserted [ " + totalRecordsInserted.length
						+ " ] at time [ " + LocalDateTime.now() + " ]");
			}
		}
		String updateQuery = "update engage_execution_history set completed_at = ? where exe_id = ?";
		jdbcTemplate.update(updateQuery, LocalDateTime.now(), exid);

	}

	private NotificationStatus getNotificationStatus(String operatingSystem, final Campaign campaign,
			final CustomObject customObject) {

		String response = null;

		Customer customer = customObject.getCustomer();
		executionMIS lst = customObject.getMis();
		String body = "";
		String header = "";

		for (int i = 0; i < campaign.getCampaignMessages().size(); i++) {
			if (customer.getLanguage().equals(campaign.getCampaignMessages().get(i).getLanguageCode())) {
				body = campaign.getCampaignMessages().get(i).getBody();
				header = campaign.getCampaignMessages().get(i).getHeader();
			}
		}

		if (body.contains("&name&")) {
			body = body.replace("&name&", customer.getFullName());
		}

		String params = lst.getParams();

		log.info("DEEP LINK URL {}", campaign.getDeepLinkURL());

		if (params != null) {
			String[] paramsArray = params.split(";");
			ArrayList<String> params_list = new ArrayList<>(Arrays.asList(paramsArray));

			body = replacePlaceholders(body, params_list);

		}

		// Email sender
		String emailId = customer.getEmailId();

		if (!emailId.equals(null) && !emailId.isEmpty()) {

			try {
				String htmlBody;
				if (("SILENT").equals(campaign.getType())) {
					htmlBody = "<html><body>" + "<h2>" + header + "</h2>" + "<p>" + body + "</p>" + "</body></html>";
				} else {
					htmlBody = "<html><body>" + "<h2>" + header + "</h2>" + "<p>" + body + "</p>"
							+ "<img src='cid:imageId'>" + "</body></html>";
				}

				MimeMessage mimeMessage = emailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				helper.setTo(emailId);
				helper.setSubject(header);
				helper.setText(htmlBody, true); // Set the content as HTML

				if (("RICH").equals(campaign.getType())) {
					ClassPathResource image = new ClassPathResource(campaign.getImage_url());
					helper.addInline("imageId", image);
				}

				emailSender.send(mimeMessage);

			} catch (MailException e) {
				log.info(e.toString());

			} catch (MessagingException e) {
				log.info(e.toString());
			}

		}
//   commented the coder for local testing disabled the firebase communication

		switch (operatingSystem) {
		case "android":

			if (("SILENT").equals(campaign.getType())) {
				response = messagingService
						.sendAndroidMessage(Message.builder().putData("uri", campaign.getDeepLinkURL())
								.setNotification(Notification.builder().setBody(body).setTitle(header).build())
								.setToken(customer.getDeviceId()).build(), campaign, customer);
			} else {
				Map<String, String> data = new HashMap<>();
				data.put("title", header);
				data.put("body", body);
				data.put("url", campaign.getImage_url());

				response = messagingService
						.sendAndroidMessage(Message.builder().putData("uri", campaign.getDeepLinkURL())

								.setNotification(Notification.builder().setBody(body).setTitle(header)
										.setImage(campaign.getImage_url()).build())
								.putAllData(data).setToken(customer.getDeviceId()).build(), campaign, customer);
			}
			return (buildNotificationStatus(response));
		case "iphone":

			return buildNotificationStatus(
					messagingService.sendIosMessage(customer.getDeviceId(), campaign.getBody(), campaign.getTitle()));

		case "webapp":

			if (("SILENT").equals(campaign.getType())) {
				response = messagingService
						.sendWebAppMessages(Message.builder().putData("uri", campaign.getDeepLinkURL())
								.setNotification(Notification.builder().setBody(body).setTitle(header).build())
								.setToken(customer.getDeviceId()).build());
			} else {

				response = messagingService.sendWebAppMessages(Message.builder()
						.putData("uri", campaign.getDeepLinkURL()).setNotification(Notification.builder().setBody(body)
								.setTitle(header).setImage(campaign.getImage_url()).build())
						.setToken(customer.getDeviceId()).build());
			}

			String res = response.replace("messages/", "messages/0:");

			log.info(res);

			return (buildNotificationStatus(res));

		default:
			return NotificationStatus.builder().status(Status.builder().code("FAILED").desc("INVALID OS").build())
					.build();
		}
		// return (buildNotificationStatus(response));
		// return (buildNotificationStatus("testing:Success"));
	}

	// Function to replace anything between & & with values from the list
	private static String replacePlaceholders(String original, ArrayList<String> replacements) {
		String replaced = original;

		// Use a regular expression to find anything between & &
		Pattern pattern = Pattern.compile("&(.*?)&");
		Matcher matcher = pattern.matcher(replaced);

		// Iterate through matches and replace with values from the list
		int index = 0;
		while (matcher.find()) {
			if (index < replacements.size()) {
				replaced = replaced.replace(matcher.group(), replacements.get(index));
				index++;
			}
		}
		return replaced;
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

	public static int countMatches(String input) {
		String regex = "&(.*?)&";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);

		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

}
