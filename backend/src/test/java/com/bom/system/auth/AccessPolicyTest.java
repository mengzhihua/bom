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

    @Test
    void plannerCanWriteOnlyPlantsAndWorkstationsInMasterData() {
        assertTrue(AccessPolicy.allows(
                User.PLANNER,
                "POST",
                "/api/master/plants"));
        assertTrue(AccessPolicy.allows(
                User.PLANNER,
                "PUT",
                "/api/master/workstations/1"));
        assertFalse(AccessPolicy.allows(
                User.PLANNER,
                "POST",
                "/api/master/vehicle-models"));
        assertFalse(AccessPolicy.allows(
                User.PLANNER,
                "POST",
                "/api/master/suppliers"));
        assertFalse(AccessPolicy.allows(
                User.PLANNER,
                "POST",
                "/api/master/features"));
        assertTrue(AccessPolicy.allows(
                User.ENGINEER,
                "POST",
                "/api/master/vehicle-models"));
        assertTrue(AccessPolicy.allows(
                User.ENGINEER,
                "POST",
                "/api/master/features"));
    }

    @Test
    void viewersCanPostReadOnlyBomOperations() {
        assertTrue(AccessPolicy.allows(
                User.VIEWER,
                "POST",
                "/api/boms/1/configure"));
        assertTrue(AccessPolicy.allows(
                User.VIEWER,
                "POST",
                "/api/boms/compare"));
        assertFalse(AccessPolicy.allows(
                User.VIEWER,
                "POST",
                "/api/boms/1/release"));
        assertFalse(AccessPolicy.allows(
                User.PLANNER,
                "POST",
                "/api/master/suppliers/1/export"));
        assertFalse(AccessPolicy.allows(
                User.VIEWER,
                "PUT",
                "/api/boms/1/configure"));
        assertFalse(AccessPolicy.allows(
                User.VIEWER,
                "DELETE",
                "/api/boms/1/configure"));
    }
}
