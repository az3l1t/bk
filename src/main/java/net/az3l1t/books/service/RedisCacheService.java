package net.az3l1t.books.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.redis.ttl}")
    private long ttlInSeconds;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "redisCache", fallbackMethod = "saveToCacheFallback")
    public void saveToCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, ttlInSeconds, TimeUnit.SECONDS);
    }

    @Async("cacheTaskExecutor")
    public void saveToCacheAsync(String key, Object value) {
        saveToCache(key, value);
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getFromCacheFallback")
    public Object getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void clearCache(String key) {
        redisTemplate.delete(key);
    }

    @Async("cacheTaskExecutor")
    public void clearCacheAsync(String key) {
        clearCache(key);
    }

    private void saveToCacheFallback(String key, Object value, Throwable t) {
        log.warn("Failed to save to Redis cache for key={}, error={}. Skipping cache save.", key, t.getMessage());
    }

    private Object getFromCacheFallback(String key, Throwable t) {
        log.warn("Failed to get from Redis cache for key={}, error={}. Returning null.", key, t.getMessage());
        return null;
    }
}