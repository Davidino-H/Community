package com.bowei.community.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Set the serialization method of the key
        template.setKeySerializer(RedisSerializer.string());
        // Set the serialization method of the value
        template.setValueSerializer(RedisSerializer.json());
        // Set the serialization method of the key of the hash
        template.setHashKeySerializer(RedisSerializer.string());
        // Set the serialization method of the value of the hash
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
