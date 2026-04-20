package ru.flux.flux.messenger.services;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.exceptions.RegistrationTokenExpiredException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "test-secret-key-for-unit-tests-must-be-long-enough-to-satisfy-hmac-sha256");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 900_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 604_800_000L);
        ReflectionTestUtils.setField(jwtService, "registrationExpirationMs", 600_000L);
    }

    private User buildUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .username("alice123")
                .phone("+12345678901")
                .password("x")
                .notifications(true)
                .build();
        return user;
    }

    @Test
    void generateAndExtractAccessToken() {
        User user = buildUser();
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractSubject(token)).isEqualTo(user.getPhone());
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void generateRefreshTokenContainsSubject() {
        User user = buildUser();
        String token = jwtService.generateRefreshToken(user);
        assertThat(jwtService.extractSubject(token)).isEqualTo(user.getPhone());
    }

    @Test
    void registrationTokenHasPurposeClaim() {
        String token = jwtService.generateRegistrationToken("subject:x", Map.of("providerUserId", "123"));

        Claims claims = jwtService.parseRegistrationToken(token);
        assertThat(claims.getSubject()).isEqualTo("subject:x");
        assertThat(claims.get("providerUserId")).isEqualTo("123");
        assertThat(claims.get(JwtService.PURPOSE_CLAIM)).isEqualTo(JwtService.PURPOSE_OAUTH_REGISTER);
    }

    @Test
    void parseRegistrationTokenRejectsAccessToken() {
        User user = buildUser();
        String accessToken = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.parseRegistrationToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a registration token");
    }

    @Test
    void parseRegistrationTokenRejectsExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "registrationExpirationMs", -1_000L);
        String expired = jwtService.generateRegistrationToken("sub", Map.of());

        assertThatThrownBy(() -> jwtService.parseRegistrationToken(expired))
                .isInstanceOf(RegistrationTokenExpiredException.class);
    }

    @Test
    void parseRegistrationTokenRejectsMalformed() {
        assertThatThrownBy(() -> jwtService.parseRegistrationToken("not-a-jwt"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
