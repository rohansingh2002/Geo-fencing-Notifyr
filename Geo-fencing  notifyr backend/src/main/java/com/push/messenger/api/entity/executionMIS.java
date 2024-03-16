package com.push.messenger.api.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="engage_execution_mis")
@AllArgsConstructor
@NoArgsConstructor
public class executionMIS {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="customer_id")
	private String customerId;
	@Column(name="exe_id")
	 private String exeId;
	
	 @Column(name = "updated_at")
	  
//	 @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
	 private LocalDateTime updatedAt;
	 @Column(name="notification_read")
	 private String notificationRead;
	 @Column(name="delivered_status")
	 private String deliveredStatus;
	 @Column(name="response_id")
	 private String responseId;
	 
	 @Column(name = "params")
	 private String params;

}

