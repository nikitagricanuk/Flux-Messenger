package ru.flux.flux.messenger.services.oauth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.dto.GoogleOAuthRequest;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;
import ru.flux.flux.messenger.exceptions.OAuthVerificationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock
    private GoogleTokenVerifier verifier;

    @Mock
    private OAuthLoginService loginService;

    @InjectMocks
    private GoogleOAuthService service;

    @Test
    void happyPathDelegatesToLoginService() {
        OAuthUserInfo info = new OAuthUserInfo("g-1", "bob@example.com", "Bob", null, null);
        when(verifier.verify("id-token")).thenReturn(info);
        OAuthLoginResponse expected = OAuthLoginResponse.builder()
                .status(OAuthLoginResponse.STATUS_LOGGED_IN).accessToken("a").refreshToken("r").build();
        when(loginService.handle(eq(OAuthProvider.GOOGLE), any())).thenReturn(expected);

        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setIdToken("id-token");
        OAuthLoginResponse result = service.login(request);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void verifierFailurePropagates() {
        when(verifier.verify("bad")).thenThrow(new OAuthVerificationException("invalid"));
        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setIdToken("bad");

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(OAuthVerificationException.class);
    }
}
