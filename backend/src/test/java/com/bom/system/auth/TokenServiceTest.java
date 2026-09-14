package com.bom.system.auth;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNull;

class TokenServiceTest {
    private final TokenService service = new TokenService(
            "test-secret",
            Duration.ofHours(1));

    @Test
    void malformedBodyReturnsNull() {
        String body = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("not-a-number:user:future".getBytes());
        assertNull(service.parse(body + ".invalid"));
    }

    @Test
    void malformedBase64ReturnsNull() {
        assertNull(service.parse("not-valid!.signature"));
    }

    @Test
    void expiredTokenReturnsNull() {
        String token = service.issue(1L, "user", 1L);
        assertNull(service.parse(token));
    }
}
