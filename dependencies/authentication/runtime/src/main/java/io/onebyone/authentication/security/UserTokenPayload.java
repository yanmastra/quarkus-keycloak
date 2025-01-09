package io.onebyone.authentication.security;

import java.util.Map;
import java.util.Set;

public interface UserTokenPayload {
    String getId();
    String getUsername();
    String getEmail();
    String getFullName();
    Set<String> getPermission();
    Map<String, Object> getAttributes();
}
