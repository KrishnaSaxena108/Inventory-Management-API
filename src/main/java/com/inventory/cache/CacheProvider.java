package com.inventory.cache;

/**
 * Abstraction over a single cache backend (Redis, LRU, Caffeine).
 *
 * <p>Every operation is failure tolerant: a backend error must never
 * propagate to the caller. {@link #get(String)} returns {@code null} on a
 * cache miss or backend failure so callers can transparently fall back to
 * the DAO.
 */
public interface CacheProvider {

    Object get(String key);

    void put(String key, Object value);

    void evict(String key);

    String type();
}
