package io.yanmastra.quarkus.rediscache.it;

import io.yanmastra.authentication.payload.UserTokenPayload;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TestUser implements UserTokenPayload {
    private final String id = UUID.randomUUID().toString();

    @Override public String getId() { return id; }
    @Override public String getUsername() { return "redis-tester"; }
    @Override public String getEmail() { return "redis-tester@example.com"; }
    @Override public String getFullName() { return "Redis Tester"; }
    @Override public Set<String> getPermission() { return Set.of("view_all"); }
    @Override public Map<String, Object> getAttributes() { return Map.of(); }
}
