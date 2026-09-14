package com.bom.system.auth;

import com.bom.system.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessPolicyTest {
    @Test
    void systemEndpointsAreAdminOnly() {
        assertTrue(AccessPolicy.allows(User.ADMIN, "GET", "/api/system/user/page"));
        assertFalse(AccessPolicy.allows(User.VIEWER, "GET", "/api/system/user/page"));
        assertFalse(AccessPolicy.allows(User.ENGINEER, "POST", "/api/system/user"));
    }

    @Test
    void nonAdminUsersCanReadBusinessEndpoints() {
        assertTrue(AccessPolicy.allows(User.VIEWER, "GET", "/api/boms"));
        assertFalse(AccessPolicy.allows(User.VIEWER, "POST", "/api/boms"));
        assertTrue(AccessPolicy.allows(User.ENGINEER, "POST", "/api/boms"));
        assertTrue(AccessPolicy.allows(User.PLANNER, "POST", "/api/boms"));
    }
}
