package com.push.messenger.api.threadPool;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.push.messenger.api.configuration.ThreadPoolConfig;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionThreadPool {
	
    private ExecutorService service;
    private final ThreadPoolConfig threadPoolConfig;
    
    public void executeRunnableTask(Runnable runnable) {
        service.execute(runnable);
    }
    
    @PostConstruct
    public void load() {
        service = Executors.newFixedThreadPool(threadPoolConfig.getSize(), r -> new Thread(r, UUID.randomUUID().toString()));
    }
    
    @PreDestroy
    @SneakyThrows
    public void cleanUpPool() {
        service.shutdown();
        boolean isTerminated = service.awaitTermination(1, TimeUnit.SECONDS);
        log.debug("is the thread pool is terminated successfully " + isTerminated);
        if (service.isShutdown()) {
            service.shutdownNow();
        }
    }

}
