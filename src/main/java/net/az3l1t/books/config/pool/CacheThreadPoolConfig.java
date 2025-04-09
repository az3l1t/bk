package net.az3l1t.books.config.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class CacheThreadPoolConfig {
    @Value("${cache.pool.core-size}")
    private int corePoolSize;

    @Value("${cache.pool.max-size}")
    private int maxPoolSize;

    @Value("${cache.pool.queue-capacity}")
    private int queueCapacity;

    @Value("${cache.pool.thread-name-prefix}")
    private String threadNamePrefix;

    @Bean(name = "cacheTaskExecutor")
    public ThreadPoolTaskExecutor cacheTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
