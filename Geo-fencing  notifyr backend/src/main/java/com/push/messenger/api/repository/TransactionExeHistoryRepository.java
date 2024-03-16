package com.push.messenger.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.push.messenger.api.entity.TransactionExeHistory;

public interface TransactionExeHistoryRepository extends JpaRepository<TransactionExeHistory, String> {
	
	List<TransactionExeHistory> findByExecutionStatus(int status, Pageable pageable);
    
    List<TransactionExeHistory> findByExecutionStatusAndScheduledAtLessThanEqual(int status, LocalDateTime currentTime, Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query("update TransactionExeHistory p set p.executionStatus = :execution_status where p.exeId in (:exe_id)")
    int updateTransactionExeHistoryBasedOnExecutionStatusAndExeIdIn(@Param("execution_status") int executionStatus, @Param("exe_id") List<String> exeId);

    @Modifying(flushAutomatically = true)
    @Query("update TransactionExeHistory p set p.executionStatus = :execution_status , p.executedTime = :executed_Time where p.exeId in (:exe_id)")
    void updateTransactionExeHistoryBasedOnExecutionStatusAndExeIdInWithCurrentDate(@Param("execution_status") int executionStatus, @Param("exe_id") List<String> exeId, @Param("executed_Time") LocalDateTime localDate);
    

}
