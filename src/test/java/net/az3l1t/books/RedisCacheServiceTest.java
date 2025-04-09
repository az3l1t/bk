package net.az3l1t.books;

import net.az3l1t.books.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field ttlField = RedisCacheService.class.getDeclaredField("ttlInSeconds");
        ttlField.setAccessible(true);
        ttlField.set(redisCacheService, 3600L);
    }

    @Test
    void saveToCache_SavesValueSuccessfully() {
        String key = "testKey";
        Object value = "testValue";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisCacheService.saveToCache(key, value);

        verify(valueOperations, times(1)).set(key, value, 3600, TimeUnit.SECONDS);
    }

    @Test
    void saveToCacheAsync_CallsSaveToCache() {
        String key = "testKey";
        Object value = "testValue";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisCacheService.saveToCacheAsync(key, value);

        verify(valueOperations, times(1)).set(key, value, 3600, TimeUnit.SECONDS);
    }

    @Test
    void getFromCache_ReturnsCachedValue() {
        String key = "testKey";
        Object expectedValue = "testValue";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(expectedValue);

        Object result = redisCacheService.getFromCache(key);

        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void clearCache_DeletesKey() {
        String key = "testKey";

        redisCacheService.clearCache(key);

        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    void getFromCache_ReturnsNull_WhenKeyNotFound() {
        String key = "nonExistentKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        Object result = redisCacheService.getFromCache(key);

        assertNull(result);
        verify(valueOperations, times(1)).get(key);
    }
}