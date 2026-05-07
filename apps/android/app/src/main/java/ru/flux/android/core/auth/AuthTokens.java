package ru.flux.android.core.auth;

public class AuthTokens {
    private String accessToken;
    private String refreshToken;

    public AuthTokens() {}

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
