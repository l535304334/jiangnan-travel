package com.jiangnan.travel.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Spring Cache 的 Redis 序列化配置。
 * 必须存在：默认 JDK 序列化无法处理未实现 Serializable 的 Result 包装对象；
 * 且缓存值含 LocalDate/LocalDateTime（如 dashboard 7 日趋势），需注册 JavaTimeModule。
 */
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 缓存需还原具体类型（值均为本应用写入，来源可信）
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(om);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("carTypes",
                        config.entryTtl(Duration.ofHours(24)))
                .withCacheConfiguration("landmarks",
                        config.entryTtl(Duration.ofHours(12)))
                .withCacheConfiguration("cityQuotes",
                        config.entryTtl(Duration.ofHours(12)))
                .withCacheConfiguration("hotDestinations",
                        config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("dashboard",
                        config.entryTtl(Duration.ofMinutes(2)))
                .build();
    }
}
