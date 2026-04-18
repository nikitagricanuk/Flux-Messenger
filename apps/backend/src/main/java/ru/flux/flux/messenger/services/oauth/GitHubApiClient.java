package ru.flux.flux.messenger.services.oauth;

public interface GitHubApiClient {
    OAuthUserInfo exchange(String code, String codeVerifier, String redirectUri);
}
