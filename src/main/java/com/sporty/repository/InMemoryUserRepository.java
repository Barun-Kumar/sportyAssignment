package com.sporty.repository;

import com.sporty.model.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Component
public class InMemoryUserRepository {
    Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);
    public static final Map<String, UserDTO> userMap = new ConcurrentHashMap<>();
    /**
     * Create a new user; fails if id already exists.
     */
    public UserDTO create(UserDTO user) {
        Objects.requireNonNull(user, "user");
        UserDTO prev = userMap.putIfAbsent(user.getUserId(), user);
        if (prev != null) {
            throw new IllegalStateException("User already exists: " + user.getUserId());
        }
        user.setUserId(UUID.randomUUID().toString());
        logger.info("user created :{}", user.getUserId());
        return user;
    }

    /**
     * Insert or replace (upsert).
     */
    public UserDTO update(UserDTO user) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(user.getUserId(), "email");
        userMap.put(user.getUserId(), user);
        logger.info("User Updated :{},"+user.getUserId());
        return user;
    }

    /**
     * Get by id.
     */
    public Optional<UserDTO> get(String userId) {
        return Optional.ofNullable(userMap.get(userId));
    }

    /**
     * Get if present; otherwise create via supplier (atomic).
     */
    public UserDTO getOrCreate(String emailId, Supplier<UserDTO> factory) {
        Objects.requireNonNull(emailId, "userId");
        Objects.requireNonNull(factory, "factory");
        return userMap.computeIfAbsent(emailId, k -> {
            UserDTO u = factory.get();
            if (u == null) throw new IllegalStateException("factory returned null");
            if (!Objects.equals(emailId, u.getUserId())) {
                throw new IllegalArgumentException("factory user.id must equal requested userId");
            }
            return u;
        });
    }


    /**
     * Delete by id, returns true if something was removed.
     */
    public boolean delete(String emailId) {
        return userMap.remove(emailId) != null;
    }

    /**
     * Exists check.
     */
    public boolean exists(String emailId) {
        return userMap.containsKey(emailId);
    }

    /**
     * Count users.
     */
    public long count() {
        return userMap.size();
    }

    /**
     * List all users (snapshot).
     */
    public List<UserDTO> list() {
        return new ArrayList<>(userMap.values());
    }

    /**
     * Find all matching a predicate.
     */
    public List<UserDTO> findAll(Predicate<UserDTO> filter) {
        Objects.requireNonNull(filter, "filter");
        return userMap.values().stream().filter(filter).toList();
    }

    /**
     * Clear everything (dangerous in prod; fine for tests).
     */
    public void clear() {
        userMap.clear();
    }

    static {
            UserDTO u1 = new UserDTO();
            u1.setUserId("varun@sporty.test");
            u1.setName("Varun Vishwakarma");
            u1.setBalance(new java.math.BigDecimal("1000.00"));
            u1.setCurrency("EUR");
            userMap.put(u1.getUserId(), u1);

            UserDTO u2 = new UserDTO();
            u2.setUserId("deeksha@sporty.test");
            u2.setName("Deeksha Sharma");
            u2.setCurrency("INR");
            u2.setBalance(new java.math.BigDecimal("1000.00"));
            userMap.put(u2.getUserId(), u2);

            UserDTO u3 = new UserDTO();
            u3.setUserId("guest1@sporty.test");
            u3.setName("Guest One");
            u3.setCurrency("INR");
            u3.setBalance(new java.math.BigDecimal("1000.00"));
            userMap.put(u3.getUserId(), u3);

    }

}
