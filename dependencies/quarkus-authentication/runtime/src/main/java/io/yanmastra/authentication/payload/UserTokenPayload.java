package io.yanmastra.authentication.payload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserTokenPayload {
    String getId();
    String getUsername();
    String getEmail();
    String getFullName();
    Set<String> getPermission();
    Map<String, Object> getAttributes();
    default List<String> getTenantAccess() {
        return new ArrayList<>();
    }

    default String getCurrentTenant() {
        return null;
    }
}
