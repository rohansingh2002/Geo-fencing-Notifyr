package com.push.messenger.api.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "transaction_execution_history")
@Getter
@Setter
@ToString
public class TransactionExeHistory {

	@Id
    @Column(name = "exe_id", nullable = false)
    private String exeId;
	
	@Column(name = "message", nullable = false)
    private String message;

    @Column(name = "executed_Time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
    private LocalDateTime executedTime;
    
    @Column(name = "scheduled_at", nullable = true)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
    private LocalDateTime scheduledAt;

    @Column(name = "execution_Status", nullable = true)
    private Integer executionStatus;

    @Column(name = "title")
    private String title;
    
//    private String customerId;

    @Column(name = "created_at")
    @CreatedDate
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
    private LocalDateTime createdAt;

//    @Column(name = "customer_id")
   // private String customerId;
    
    @Column(name = "user_id")
	private String userId;
    
    @Column(name="completed_At", nullable = true)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
    @CreatedBy
	private LocalDateTime completedAt;
    
    @Column
    private String segmentName;
    
    @Column(name = "image_url")
	private String imageUrl;

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj)) return false;
//        ExecutionHistory that = (ExecutionHistory) obj;
//        return exeId != null && Objects.equals(exeId, that.exeId);
//    }

//    @Override
//    public int hashCode() {
//        return getClass().hashCode();
//    }
//
//    @Override
//    public boolean isNew() {
//        return true;
//    }

//	@Override
//	public String getId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
