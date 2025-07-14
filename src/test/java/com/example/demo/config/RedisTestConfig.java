package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestConfiguration
public class RedisTestConfig {


    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplateTest(){
        RedisTemplate<String,String > redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String,String>valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn("MOCK_CODE");
        doNothing().when(valueOps).set(anyString(), anyString(), any());
        return redisTemplate;
    }

}
