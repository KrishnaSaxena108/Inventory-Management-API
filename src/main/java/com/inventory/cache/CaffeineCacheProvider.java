package com.inventory.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("caffeineCacheProvider")
public class CaffeineCacheProvider implements CacheProvider {

    private static final Logger log =
            LoggerFactory.getLogger(CaffeineCacheProvider.class);

    private final Cache<String, Object> cache;

    public CaffeineCacheProvider(
            @Value("${app.cache.caffeine.max-size:500}") long maxSize,
            @Value("${app.cache.caffeine.ttl-seconds:300}") long ttlSeconds) {

        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .build();
    }

    @Override
    public Object get(String key) {
        try {
            return cache.getIfPresent(key);
        } catch (Exception ex) {
            log.warn("Caffeine GET failed for key {}: {}",
                    key, ex.getMessage());
            return null;
        }
    }

    @Override
    public void put(String key, Object value) {
        if (value == null) {
            return;
        }
        try {
            cache.put(key, value);
        } catch (Exception ex) {
            log.warn("Caffeine PUT failed for key {}: {}",
                    key, ex.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        try {
            cache.invalidate(key);
        } catch (Exception ex) {
            log.warn("Caffeine EVICT failed for key {}: {}",
                    key, ex.getMessage());
        }
    }

    @Override
    public String type() {
        return "CAFFEINE";
    }
}
