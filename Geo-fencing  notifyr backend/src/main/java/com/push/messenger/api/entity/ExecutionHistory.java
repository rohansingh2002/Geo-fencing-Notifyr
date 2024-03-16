package com.push.messenger.api.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Engage_Execution_History")
@Getter
@Setter
@ToString
public class ExecutionHistory implements Persistable<String>   {

    @Id
    @Column(name = "exe_id", nullable = false)
    private String exeId;

    @Column(name = "campaign_id", nullable = false)
    private String campaignId;

    @Column(name = "executed_Time")
    private LocalDateTime executedTime;

    @Column(name = "execution_status", nullable = true)
    private Integer executionStatus;
    
    @Column(name = "scheduled_at", nullable = true)
    private LocalDateTime scheduledAt;

    @Column(name = "campaign_type")
    private String campaignType;
    
//    private String customerId;

//    @Column(name = "created_at")
//    @CreatedDate
//    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

//    @Column(name = "customer_id")
   // private String customerId;
    
    @Column(name = "user_id")
	private String userId;
    
//    @Column(name="completed_At", nullable = true)
//    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "Asia/Kolkata")
//    @CreatedBy
//	
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj)) return false;
        ExecutionHistory that = (ExecutionHistory) obj;
        return exeId != null && Objects.equals(exeId, that.exeId);
    }

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return true;
	}

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
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
//@Entity
//@Table(name = "push_notification")
//@Getter
//@Setter
//@ToString
//public class PushNotification implements Persistable<String> {
//
//    @Id
//    @Column(name = "id", nullable = false)
//    private String id;
//
//    @Column(name = "campaign_type", nullable = false)
//    private String campaignType;
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "status", nullable = false)
//    private Integer status;
//
//    @Column(name = "campaign_id", nullable = false)
//    private String campaignId;
//
//    @Column(name = "completed_by")
//    private LocalDateTime completedBy;
//
//    @Column(name = "customer_id")
//    private String customerId;
//
//    @Column(name = "name")
//    private String name;
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
//        PushNotification that = (PushNotification) o;
//        return id != null && Objects.equals(id, that.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return getClass().hashCode();
//    }
//
//    @Override
//    public boolean isNew() {
//        return true;
//    }
//}
