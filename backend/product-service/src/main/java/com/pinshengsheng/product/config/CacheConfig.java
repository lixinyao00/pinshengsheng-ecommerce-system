package com.pinshengsheng.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class CacheConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService productCacheExecutor() {
        return Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task);
            thread.setName("product-cache-delete");
            thread.setDaemon(true);
            return thread;
        });
    }
}
