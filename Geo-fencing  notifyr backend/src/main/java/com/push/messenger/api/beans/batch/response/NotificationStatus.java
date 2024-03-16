package com.push.messenger.api.beans.batch.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificationStatus {
    private Status status;
    
    
}
