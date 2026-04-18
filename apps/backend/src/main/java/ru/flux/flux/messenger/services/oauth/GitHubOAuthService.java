package ru.flux.flux.messenger.services.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.dto.GitHubOAuthRequest;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;

@Service
@RequiredArgsConstructor
public class GitHubOAuthService {
    private final GitHubApiClient client;
    private final OAuthLoginService loginService;

    public OAuthLoginResponse login(GitHubOAuthRequest request) {
        OAuthUserInfo info = client.exchange(request.getCode(), request.getCodeVerifier(), request.getRedirectUri());
        return loginService.handle(OAuthProvider.GITHUB, info);
    }
}
