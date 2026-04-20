package ru.flux.flux.messenger.services.oauth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.OAuthCompleteRequest;
import ru.flux.flux.messenger.exceptions.RegistrationTokenExpiredException;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthCompletionServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Claims claims;

    @InjectMocks
    private OAuthCompletionService service;

    private OAuthCompleteRequest request;

    @BeforeEach
    void setUp() {
        request = new OAuthCompleteRequest();
        request.setRegistrationToken("reg-token");
        request.setPhone("+12345678901");
        request.setUsername("alice");
    }

    @Test
    void happyPathCreatesUserAndReturnsJwt() {
        when(jwtService.parseRegistrationToken("reg-token")).thenReturn(claims);
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER)).thenReturn("GOOGLE");
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER_USER_ID)).thenReturn("g-1");
        when(claims.get(OAuthLoginService.CLAIM_EMAIL)).thenReturn("alice@example.com");
        when(claims.get(OAuthLoginService.CLAIM_FIRST_NAME)).thenReturn("Alice");
        when(claims.get(OAuthLoginService.CLAIM_LAST_NAME)).thenReturn(null);
        when(claims.get(OAuthLoginService.CLAIM_AVATAR_URL)).thenReturn(null);
        when(userService.findLinkedUser(OAuthProvider.GOOGLE, "g-1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        User saved = User.builder()
                .id(UUID.randomUUID()).firstName("Alice").username("alice")
                .phone("+12345678901").password("encoded").notifications(true).build();
        when(userService.createOAuthUser(eq(OAuthProvider.GOOGLE), eq("g-1"), eq("alice@example.com"),
                eq("+12345678901"), eq("alice"), eq("Alice"), eq(null), eq(null), eq("encoded")))
                .thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("access");
        when(jwtService.generateRefreshToken(saved)).thenReturn("refresh");

        JwtAuthenticationResponse response = service.complete(request);
        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
    }

    @Test
    void requestFirstNameOverridesClaim() {
        request.setFirstName("Bob");
        when(jwtService.parseRegistrationToken("reg-token")).thenReturn(claims);
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER)).thenReturn("GITHUB");
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER_USER_ID)).thenReturn("gh-1");
        when(claims.get(OAuthLoginService.CLAIM_EMAIL)).thenReturn(null);
        when(claims.get(OAuthLoginService.CLAIM_FIRST_NAME)).thenReturn("Alice");
        when(claims.get(OAuthLoginService.CLAIM_LAST_NAME)).thenReturn(null);
        when(claims.get(OAuthLoginService.CLAIM_AVATAR_URL)).thenReturn(null);
        when(userService.findLinkedUser(OAuthProvider.GITHUB, "gh-1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User saved = User.builder().id(UUID.randomUUID()).firstName("Bob").username("alice")
                .phone("+12345678901").password("encoded").notifications(true).build();
        when(userService.createOAuthUser(eq(OAuthProvider.GITHUB), eq("gh-1"), any(),
                any(), any(), eq("Bob"), any(), any(), any())).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("a");
        when(jwtService.generateRefreshToken(saved)).thenReturn("r");

        service.complete(request);
        verify(userService).createOAuthUser(eq(OAuthProvider.GITHUB), eq("gh-1"), any(),
                any(), any(), eq("Bob"), any(), any(), any());
    }

    @Test
    void alreadyLinkedUserSkipsCreation() {
        when(jwtService.parseRegistrationToken("reg-token")).thenReturn(claims);
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER)).thenReturn("GOOGLE");
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER_USER_ID)).thenReturn("g-1");
        User existing = User.builder().id(UUID.randomUUID()).firstName("A").username("a")
                .phone("+12345678901").password("x").notifications(true).build();
        when(userService.findLinkedUser(OAuthProvider.GOOGLE, "g-1")).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(existing)).thenReturn("access");
        when(jwtService.generateRefreshToken(existing)).thenReturn("refresh");

        JwtAuthenticationResponse response = service.complete(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(userService, org.mockito.Mockito.never()).createOAuthUser(any(), any(), any(),
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void expiredRegistrationTokenPropagates() {
        when(jwtService.parseRegistrationToken("reg-token"))
                .thenThrow(new RegistrationTokenExpiredException("expired"));

        assertThatThrownBy(() -> service.complete(request))
                .isInstanceOf(RegistrationTokenExpiredException.class);
    }

    @Test
    void invalidProviderClaimRejected() {
        when(jwtService.parseRegistrationToken("reg-token")).thenReturn(claims);
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER)).thenReturn("ATLASSIAN");

        assertThatThrownBy(() -> service.complete(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingProviderUserIdRejected() {
        when(jwtService.parseRegistrationToken("reg-token")).thenReturn(claims);
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER)).thenReturn("GOOGLE");
        when(claims.get(OAuthLoginService.CLAIM_PROVIDER_USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> service.complete(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
