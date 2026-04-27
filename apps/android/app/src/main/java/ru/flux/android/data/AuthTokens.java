package ru.flux.android.data;

public class AuthTokens {
    private String accessToken;
    private String refreshToken;
    private String userId;
    public AuthTokens() {}

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUserId() { return userId; }
}
