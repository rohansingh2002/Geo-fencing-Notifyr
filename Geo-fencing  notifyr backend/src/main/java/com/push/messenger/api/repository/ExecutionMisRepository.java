package com.push.messenger.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.push.messenger.api.beans.batch.customer.Customer;
import com.push.messenger.api.entity.executionMIS;

@Repository
public interface ExecutionMisRepository extends JpaRepository<executionMIS, Long> {

	List<executionMIS> findByExeId(String exeId);

//	List<Customer> findAllByExeId(String exid);
	
	

}
