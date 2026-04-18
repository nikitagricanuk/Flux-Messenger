package ru.flux.flux.messenger.services.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.dto.GoogleOAuthRequest;
import ru.flux.flux.messenger.dto.OAuthLoginResponse;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    private final GoogleTokenVerifier verifier;
    private final OAuthLoginService loginService;

    public OAuthLoginResponse login(GoogleOAuthRequest request) {
        OAuthUserInfo info = verifier.verify(request.getIdToken());
        return loginService.handle(OAuthProvider.GOOGLE, info);
    }
}
