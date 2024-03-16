package com.push.messenger.api.beans.batch.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class BatchResponse {
    private List<String> id;

}
