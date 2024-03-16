package com.push.messenger.api.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pool.thread")
@Data
public class ThreadPoolConfig {
    private int size;
}
