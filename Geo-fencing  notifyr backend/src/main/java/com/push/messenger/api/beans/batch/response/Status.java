package com.push.messenger.api.beans.batch.response;

import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Status {
    private String code;
    private String desc;
}
