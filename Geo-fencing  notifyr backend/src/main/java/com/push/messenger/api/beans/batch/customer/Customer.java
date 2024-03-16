package com.push.messenger.api.beans.batch.customer;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigInteger;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class Customer {
    private String id;
    private String customerId;
    private String deviceId;
    private String mobileNumber;
    private String deviceName;
    private String emailId;
    private String is_notification_on;
    private String operationSystem;
    private String fullName;
    private String var1;
    private String var2;
    private String var3;
    private String language;
}
