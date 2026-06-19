package io.yanmastra.authentication.it;

import io.yanmastra.quarkusBase.security.UserPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserPermissionTest {

    @Test
    void implies_sameNameReturnsTrue() {
        UserPermission p = new UserPermission("view_all");
        assertTrue(p.implies(new UserPermission("view_all")));
    }

    @Test
    void implies_differentNameReturnsFalse() {
        UserPermission p = new UserPermission("view_all");
        assertFalse(p.implies(new UserPermission("manage_users")));
    }

    @Test
    void implies_blankNameReturnsFalse() {
        UserPermission p = new UserPermission("");
        assertFalse(p.implies(new UserPermission("view_all")));
    }

    @Test
    void equals_sameNameReturnsTrue() {
        assertEquals(new UserPermission("view_all"), new UserPermission("view_all"));
    }

    @Test
    void equals_differentNameReturnsFalse() {
        assertNotEquals(new UserPermission("view_all"), new UserPermission("manage_users"));
    }

    @Test
    void equals_selfReturnsTrue() {
        UserPermission p = new UserPermission("view_all");
        assertEquals(p, p);
    }

    @Test
    void equals_nullReturnsFalse() {
        assertNotEquals(null, new UserPermission("view_all"));
    }

    @Test
    void equals_differentTypeReturnsFalse() {
        assertNotEquals("view_all", new UserPermission("view_all"));
    }

    @Test
    void hashCode_sameNameProducesSameHash() {
        assertEquals(
                new UserPermission("view_all").hashCode(),
                new UserPermission("view_all").hashCode()
        );
    }

    @Test
    void hashCode_differentNameProducesDifferentHash() {
        assertNotEquals(
                new UserPermission("view_all").hashCode(),
                new UserPermission("manage_users").hashCode()
        );
    }

    @Test
    void getActions_returnsName() {
        assertEquals("view_all", new UserPermission("view_all").getActions());
    }

    @Test
    void getActions_blankNameReturnsBlank() {
        assertEquals("", new UserPermission("").getActions());
    }
}
