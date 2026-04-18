package ru.flux.flux.messenger.services.oauth;

public interface GoogleTokenVerifier {
    OAuthUserInfo verify(String idToken);
}
