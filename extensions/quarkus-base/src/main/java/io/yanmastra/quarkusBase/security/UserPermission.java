package io.yanmastra.quarkusBase.security;

import org.apache.commons.lang3.StringUtils;

import java.security.Permission;

public class UserPermission extends Permission {
    /**
     * Constructs a permission with the specified name.
     *
     * @param name name of the {@code Permission} object being created.
     *
     */
    public UserPermission(String name) {
        super(name);
    }

    @Override
    public boolean implies(Permission permission) {
        return StringUtils.isNotBlank(getName()) && getName().equals(permission.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        UserPermission other = (UserPermission) obj;
        if (getName() == null && other.getName() != null) return false;
        if (getName() == null || other.getName() == null) return false;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String getActions() {
        return getName();
    }
}
