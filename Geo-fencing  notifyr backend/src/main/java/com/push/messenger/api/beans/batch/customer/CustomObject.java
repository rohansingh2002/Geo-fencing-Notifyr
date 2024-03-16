package com.push.messenger.api.beans.batch.customer;

import com.push.messenger.api.entity.executionMIS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CustomObject {

	Customer customer;
	executionMIS mis;
}
