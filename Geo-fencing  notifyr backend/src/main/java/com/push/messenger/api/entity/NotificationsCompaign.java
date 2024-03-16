package com.push.messenger.api.entity;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@Entity
@Table(name ="Engage_Campaign")
@EntityListeners(AuditingEntityListener.class)
@EnableJpaAuditing
@Validated
@JsonAutoDetect
//@TypeDef(name = "LONGTEXT", typeClass = JsonBinaryType.class)
public class NotificationsCompaign implements Serializable {

	private static final long serialVersionUID = -2578259326385174455L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long campaignId;
	
	@Column
	private String title;
	
	@Column(length = 1024)
	private String body;
	
	@Column(name = "body_marathi",length = 1024)
	private String bodyMarathi;
	
	@Column(name = "body_hindi",length = 1024)
	private String bodyHindi;
	
	@Column
	private String header;
	
	@Column(columnDefinition = "TIMESTAMP", nullable = true, updatable =true)
	@CreatedDate
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	
	private LocalDateTime createdAt;	

	@Column(name = "image_url")
	private String imageUrl;
	
	
	
//	@Enumerated(EnumType.STRING)
//	@Column(length = 10, nullable = false)
//	private CampaignType notificationType;
	
	

	@Column(columnDefinition = "TIMESTAMP", nullable = true)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime executedAt;	
	
	
//	@Enumerated(EnumType.STRING)
//	@Column(length = 10, nullable = false)
//	private CampaignStatus	campaignStatus;
	
}
