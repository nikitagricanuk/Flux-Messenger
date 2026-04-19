package ru.flux.android.data;

public class AuthTokens {
    private String accessToken;
    private String refreshToken;

    public AuthTokens() {}

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
