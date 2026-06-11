package com.inventory.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-process, fixed-capacity cache with classic least-recently-used
 * eviction backed by an access-ordered {@link LinkedHashMap}.
 */
@Component("lruCacheProvider")
public class LruCacheProvider implements CacheProvider {

    private static final Logger log =
            LoggerFactory.getLogger(LruCacheProvider.class);

    private final Map<String, Object> cache;

    public LruCacheProvider(
            @Value("${app.cache.lru.max-size:500}") int maxSize) {

        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(
                            Map.Entry<String, Object> eldest) {
                        return size() > maxSize;
                    }
                });
    }

    @Override
    public Object get(String key) {
        try {
            return cache.get(key);
        } catch (Exception ex) {
            log.warn("LRU GET failed for key {}: {}",
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
            log.warn("LRU PUT failed for key {}: {}",
                    key, ex.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        try {
            cache.remove(key);
        } catch (Exception ex) {
            log.warn("LRU EVICT failed for key {}: {}",
                    key, ex.getMessage());
        }
    }

    @Override
    public String type() {
        return "LRU";
    }
}
