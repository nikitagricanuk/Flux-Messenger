package ru.flux.flux.messenger.services.oauth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.dto.GitHubOAuthRequest;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;
import ru.flux.flux.messenger.exceptions.OAuthVerificationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubOAuthServiceTest {

    @Mock
    private GitHubApiClient client;

    @Mock
    private OAuthLoginService loginService;

    @InjectMocks
    private GitHubOAuthService service;

    @Test
    void happyPathDelegatesToLoginService() {
        OAuthUserInfo info = new OAuthUserInfo("gh-1", null, "octocat", null, "https://avatar");
        when(client.exchange("code", "verifier", "http://x")).thenReturn(info);
        OAuthLoginResponse expected = OAuthLoginResponse.builder()
                .status(OAuthLoginResponse.STATUS_NEEDS_PROFILE).registrationToken("tok").build();
        when(loginService.handle(eq(OAuthProvider.GITHUB), any())).thenReturn(expected);

        GitHubOAuthRequest request = new GitHubOAuthRequest();
        request.setCode("code");
        request.setCodeVerifier("verifier");
        request.setRedirectUri("http://x");

        OAuthLoginResponse result = service.login(request);
        assertThat(result).isSameAs(expected);
    }

    @Test
    void clientFailurePropagates() {
        when(client.exchange(any(), any(), any())).thenThrow(new OAuthVerificationException("boom"));

        GitHubOAuthRequest request = new GitHubOAuthRequest();
        request.setCode("code");
        request.setRedirectUri("http://x");

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(OAuthVerificationException.class);
    }
}
