package com.badboys.unbound_auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 연결 설정
        return new LettuceConnectionFactory("13.125.19.17", 6379);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // RedisTemplate 설정
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer()); // 키는 String 형식
        template.setValueSerializer(new StringRedisSerializer()); // 값도 String 형식 (필요시 변경 가능)
        return template;
    }
}
