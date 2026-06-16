package com.inventory.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("redisCacheProvider")
public class RedisCacheProvider implements CacheProvider {

    private static final Logger log =
            LoggerFactory.getLogger(RedisCacheProvider.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration ttl;

    public RedisCacheProvider(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${app.cache.redis.ttl-seconds:600}") long ttlSeconds) {

        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value != null) {
                log.info("REDIS HIT  -> {}", key);
            } else {
                log.info("REDIS MISS -> {}", key);
            }

            return value;

        } catch (Exception ex) {
            log.warn("Redis GET failed for key {}: {}", key, ex.getMessage());
            return null;
        }
    }

    @Override
    public void put(String key, Object value) {

        if (value == null) {
            log.info("REDIS PUT SKIPPED -> {} (null value)", key);
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.info("REDIS PUT  -> {} (TTL = {} seconds)", key, ttl.getSeconds());

        } catch (Exception ex) {
            log.warn("Redis PUT failed for key {}: {}", key, ex.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.info("REDIS DELETE -> {}", key);

        } catch (Exception ex) {
            log.warn("Redis EVICT failed for key {}: {}", key, ex.getMessage());
        }
    }

    @Override
    public String type() {
        return "REDIS";
    }
}