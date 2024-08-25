package com.example.oliveyoung.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.lettuce.core.resource.ClientResources;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.Arrays;

@Configuration
public class RedisConfig {

    @Value("${SPRING_DATA_REDIS_HOST}")
    private String redisHost;

    @Value("${SPRING_DATA_REDIS_PORT}")
    private int redisPort;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Key는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());

        // JSON 직렬화를 위한 ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Jackson2JsonRedisSerializer에 ObjectMapper 설정
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Value와 Hash의 Value를 JSON으로 직렬화
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(
                Arrays.asList(redisHost + ":" + redisPort));

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisClusterConfiguration);
        factory.setClientResources(clientResources);

        // Connection Pool 설정 (최대 연결 수와 최소 유휴 연결 수 등 조정)
        factory.setShareNativeConnection(true);  // 여러 쓰레드 간에 연결을 공유

        return factory;
    }

    @Bean
    public SmartInitializingSingleton forceRedisConnection(LettuceConnectionFactory redisConnectionFactory) {
        // Redis 연결 강제 초기화
        return redisConnectionFactory::getConnection;
    }
}