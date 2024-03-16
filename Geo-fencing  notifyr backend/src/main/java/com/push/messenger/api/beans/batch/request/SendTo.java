package com.push.messenger.api.beans.batch.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SendTo {
    private String fullName;
    private String customerId;
}
