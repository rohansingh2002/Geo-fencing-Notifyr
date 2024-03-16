package com.push.messenger.api.configuration;

import com.digi.constant.KeyConstants;
import com.digi.encryptor.EncryptDecryptUtil;
import com.push.messenger.api.beans.batch.response.Status;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@AllArgsConstructor
@Slf4j
@Component
public class DataBaseConfig {

    private final Environment environment;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    @DependsOn("restTemplate")
    public DataSource dataSource() {
        Response response =  restTemplate().getForEntity(Objects.requireNonNull("http://localhost:8080/engage-server/credential/get/1"), Response.class).getBody();
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        if (Objects.nonNull(response) && Objects.nonNull(response.getData())) {
            dataSourceBuilder.password(EncryptDecryptUtil.decrypt(response.getData().getPassword(), KeyConstants.STR_SECRET_KEY));
            dataSourceBuilder.url(response.getData().getUrl());
            dataSourceBuilder.username(EncryptDecryptUtil.decrypt(response.getData().getUsername(),  KeyConstants.STR_SECRET_KEY));
            return dataSourceBuilder.build();
        }
        throw new RuntimeException("datasource cannot be configured properly");
    }
}

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
class Data {
    private String username;
    private String password;
    private String url;
}

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
class status {
    private String code;
    private String desc;
}

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
class Response {
    private Status status;
    private Data data;
}
