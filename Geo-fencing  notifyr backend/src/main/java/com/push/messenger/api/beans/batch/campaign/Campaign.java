package com.push.messenger.api.beans.batch.campaign;

import java.util.List;

import com.push.messenger.api.beans.enumclass.CampaignType;
import com.push.messenger.api.entity.CampaignMessages;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Campaign {
    private String id;
    private String body;
    private String body_hindi;
    private String body_marathi;
    private String type;
    private String title;
    private String header;
    private String image_url;
    private List<CampaignMessages> campaignMessages;
    private String deepLinkURL;
}
