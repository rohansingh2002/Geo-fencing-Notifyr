package com.push.messenger.api.repository;

import com.push.messenger.api.entity.ExecutionHistory;
//import com.push.messenger.api.entity.PushNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<ExecutionHistory, String> {

    List<ExecutionHistory> findByExecutionStatus(int status, Pageable pageable);
    
    List<ExecutionHistory> findByExecutionStatusAndScheduledAtLessThanEqual(int status, LocalDateTime currentTime, Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query("update ExecutionHistory p set p.executionStatus = :execution_status where p.exeId in (:exe_id)")
    int updateExecutionHistoryBasedOnExecutionStatusAndExeIdIn(@Param("execution_status") int executionStatus, @Param("exe_id") List<String> exeId);

    @Modifying(flushAutomatically = true)
    @Query("update ExecutionHistory p set p.executionStatus = :execution_status , p.executedTime = :executed_Time where p.exeId in (:exe_id)")
    void updateExecutionHistoryBasedOnExecutionStatusAndExeIdInWithCurrentDate(@Param("execution_status") int executionStatus, @Param("exe_id") List<String> exeId, @Param("executed_Time") LocalDateTime localDate);
    
    
}
