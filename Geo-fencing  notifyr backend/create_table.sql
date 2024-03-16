create table push_mis
(
    id            varchar(255)                        not null
        primary key,
    batch_id      varchar(255)                        not null,
    campaign_type varchar(255)                        not null,
    status        varchar(255)                        null,
    response_id   varchar(255)                        not null,
    customer_id   int                                 not null,
    created_at    timestamp default CURRENT_TIMESTAMP null,
    campaignid    varchar(255) not null
);


create table push_notification
(
    ID            varchar(255)                           not null
        primary key,
    campaign_type varchar(100) default 'ALL'             not null ,
    created_at    timestamp    default CURRENT_TIMESTAMP not null,
    status        int          default 0                 null ,
    campaign_id   varchar(255)                           not null,
    completed_by  timestamp                              null,
    customer_id   varchar(255)                           null
);

