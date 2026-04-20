package ru.flux.flux.messenger.services.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.UserService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private OAuthLoginService service;

    private OAuthUserInfo info;

    @BeforeEach
    void setUp() {
        info = new OAuthUserInfo("provider-123", "alice@example.com", "Alice", "Liddell", "https://avatar");
    }

    @Test
    void linkedUserGetsJwtImmediately() {
        User user = buildUser();
        when(userService.findLinkedUser(OAuthProvider.GOOGLE, "provider-123"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");

        OAuthLoginResponse response = service.handle(OAuthProvider.GOOGLE, info);

        assertThat(response.getStatus()).isEqualTo(OAuthLoginResponse.STATUS_LOGGED_IN);
        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getRegistrationToken()).isNull();
        verify(userService, org.mockito.Mockito.never()).linkOAuthIdentity(any(), any(), any(), any());
    }

    @Test
    void emailMatchAutoLinksExistingUser() {
        User user = buildUser();
        when(userService.findLinkedUser(OAuthProvider.GITHUB, "provider-123"))
                .thenReturn(Optional.empty());
        when(userService.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");

        OAuthLoginResponse response = service.handle(OAuthProvider.GITHUB, info);

        assertThat(response.getStatus()).isEqualTo(OAuthLoginResponse.STATUS_LOGGED_IN);
        verify(userService).linkOAuthIdentity(user, OAuthProvider.GITHUB, "provider-123", "alice@example.com");
    }

    @Test
    void unknownUserGetsRegistrationToken() {
        when(userService.findLinkedUser(OAuthProvider.GOOGLE, "provider-123"))
                .thenReturn(Optional.empty());
        when(userService.findByEmail("alice@example.com"))
                .thenReturn(Optional.empty());
        when(jwtService.generateRegistrationToken(anyString(), anyMap()))
                .thenReturn("reg-token");

        OAuthLoginResponse response = service.handle(OAuthProvider.GOOGLE, info);

        assertThat(response.getStatus()).isEqualTo(OAuthLoginResponse.STATUS_NEEDS_PROFILE);
        assertThat(response.getRegistrationToken()).isEqualTo("reg-token");
        assertThat(response.getSuggestedEmail()).isEqualTo("alice@example.com");
        assertThat(response.getSuggestedFirstName()).isEqualTo("Alice");
        assertThat(response.getAccessToken()).isNull();
    }

    @Test
    void nullEmailSkipsEmailLookup() {
        OAuthUserInfo noEmail = new OAuthUserInfo("p-9", null, null, null, null);
        when(userService.findLinkedUser(OAuthProvider.GITHUB, "p-9")).thenReturn(Optional.empty());
        when(jwtService.generateRegistrationToken(anyString(), anyMap())).thenReturn("reg");

        OAuthLoginResponse response = service.handle(OAuthProvider.GITHUB, noEmail);

        assertThat(response.getStatus()).isEqualTo(OAuthLoginResponse.STATUS_NEEDS_PROFILE);
        verify(userService, org.mockito.Mockito.never()).findByEmail(any());
    }

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .username("alice")
                .phone("+12345678901")
                .email("alice@example.com")
                .password("x")
                .notifications(true)
                .build();
    }
}
